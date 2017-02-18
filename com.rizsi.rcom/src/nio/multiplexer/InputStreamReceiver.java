package nio.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Multiplexer receiver that buffers the incoming data and it is possible to read the
 * incoming data from a stream.
 * 
 * This implementation can block the main NIO thread in case the receiving buffer is full!
 * 
 * @author rizsi
 *
 */
public class InputStreamReceiver extends MultiplexerReceiver
{
	private ByteBuffer bb;
	private ByteBuffer reader;
	private long nWrite;
	private long nRead;
	private long closedAt=-1;
	public Is in;
	private boolean noOverflow;
	public class Is extends InputStream
	{
		/**
		 * Get the number of bytes that can be read without blocking.
		 * @return
		 */
		@Override
		public int available() throws IOException {
			synchronized (bb) {
				return (int)(nWrite-nRead);
			}			
		}
		@Override
		public int read() throws IOException {
			try {
				synchronized (bb) {
					int cap=(int)(nWrite-nRead);
					while(cap==0)
					{
						if(nRead==closedAt)
						{
							return -1;
						}
						bb.wait();
						cap=(int)(nWrite-nRead);
					}
					nRead++;
					byte ret=reader.get();
					if(!reader.hasRemaining())
					{
						reader.clear();
					}
					if(noOverflow)
					{
						setReceiveBufferAvailable(bb.capacity()+nRead);
					}
					return 0xff&ret;
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			try {
				synchronized (bb) {
					int cap=(int)(nWrite-nRead);
					while(cap==0)
					{
						if(nRead==closedAt)
						{
							return -1;
						}
						bb.wait();
						cap=(int)(nWrite-nRead);
					}
					int l=Math.min(cap, len);
					l=Math.min(l, reader.capacity()-reader.position());
					if(l<0)
					{
						throw new IOException("Internal error");
					}
					reader.limit(reader.position()+l);
					reader.get(b, off, l);
					nRead+=l;
					if(reader.position()==reader.capacity())
					{
						reader.clear();
					}
					if(noOverflow)
					{
						setReceiveBufferAvailable(bb.capacity()+nRead);
					}
					return l;
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
	@Override
	public int read(ReadableByteChannel bc, int remainingBytes) throws IOException {
		int n;
		try {
			synchronized (bb) {
				boolean needNotify=nRead==nWrite;
				int spaceInBuffer=getPositiveSpaceInBuffer();
				int l=Math.min(remainingBytes, bb.capacity()-bb.position());
				l=Math.min(l, spaceInBuffer);
				bb.limit(l+bb.position());
				n=bc.read(bb);
				if(n>0)
				{
					nWrite+=n;
					if(needNotify)
					{
						bb.notifyAll();
					}
				}
				if(bb.position()==bb.capacity())
				{
					bb.clear();
				}
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return n;
	}
	private int getPositiveSpaceInBuffer() throws InterruptedException {
		int cap=(int)(bb.capacity()-(nWrite-nRead));
		while(cap==0)
		{
			bb.wait();
			cap=(int)(bb.capacity()-(nWrite-nRead));
		}
		return cap;
	}
	public InputStreamReceiver(int pipeSize, boolean noOverflow)
	{
		super();
		bb=ByteBuffer.allocateDirect(pipeSize);
		reader=bb.asReadOnlyBuffer();
		in=new Is();
		this.noOverflow=noOverflow;
		if(noOverflow)
		{
			setReceiveBufferAvailable(bb.capacity()+nRead);
		}
	}
	@Override
	public void close(Exception e) {
		super.close(e);
		synchronized (bb) {
			closedAt=nWrite;
			bb.notifyAll();
		}
	}
}
