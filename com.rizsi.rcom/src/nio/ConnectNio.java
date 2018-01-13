package nio;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;

public class ConnectNio {
	public static SourceChannel inputStreamToPipe(final InputStream is) throws IOException
	{
		final Pipe in=Pipe.open();
		in.sink().configureBlocking(true);
		new Thread(ConnectNio.class.getSimpleName()+" input"){
			public void run() {
				byte[] arr=new byte[8096];
				ByteBuffer bb=ByteBuffer.wrap(arr);
				try {
					while(true)
					{
						int n=is.read(arr);
						if(n<0)
						{
							in.sink().close();
							throw new EOFException();
						}
						bb.clear();
						bb.put(arr, 0, n);
						bb.flip();
						in.sink().write(bb);
					}
				} catch (EOFException e)
				{
					// EOF is the normal way to close this thread.
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
		return in.source();
	}

	public static SinkChannel outputStreamToPipe(final OutputStream os, final Closeable toClose) throws IOException {
		final Pipe out=Pipe.open();
		out.source().configureBlocking(true);
		new Thread(ConnectNio.class.getSimpleName()+" output"){
			public void run() {
				byte[] arr=new byte[8096];
				ByteBuffer bb=ByteBuffer.wrap(arr);
				try {
					while(true)
					{
						bb.clear();
						int n=out.source().read(bb);
						if(n<0)
						{
							if(toClose!=null)
							{
								toClose.close();
							}
							throw new EOFException();
						}
						os.write(arr, 0, n);
						os.flush();
					}
				} catch (EOFException e)
				{
					// This is normal shutdown.
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
		return out.sink();
	}
}
