package nio.multiplexer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

/**
 * Provide an output stream that gathers data into a buffer and 
 * sends all data through the multiplexer.
 * 
 * Writing to the stream is blocked in case the buffer is full.
 * 
 * @author rizsi
 *
 */
public class OutputStreamSender extends MultiplexerSender
{
	class SendOutputStream extends OutputStream
	{
		@Override
		public void write(int b) throws IOException {
			try {
				synchronized (writeBuffer) {
					getPositiveWriteCapacity();
					writeBuffer.limit(writeBuffer.position()+1);
					writeBuffer.put((byte) b);
					checkTurnaround(1);
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			try {
				synchronized (writeBuffer) {
					while(len>0)
					{
						int bufferCap=getPositiveWriteCapacity();
						int n=Math.min(bufferCap, writeBuffer.capacity()-writeBuffer.position());
						n=Math.min(n,  len);
						writeBuffer.limit(writeBuffer.position()+n);
						writeBuffer.put(b, off, n);
						if(n>0)
						{
							off+=n;
							len-=n;
						}
						checkTurnaround(n);
					}
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		private int getPositiveWriteCapacity() throws InterruptedException
		{
			int inbuffer=(int)(nWritten-nRead);
			int freebuffer=writeBuffer.capacity()-inbuffer;
			while(freebuffer==0)
			{
				writeBuffer.wait();
				inbuffer=(int)(nWritten-nRead);
				freebuffer=writeBuffer.capacity()-inbuffer;
			}
			return freebuffer;
		}
		@Override
		public void flush() throws IOException {
		}
	}
	public final OutputStream os;
	ByteBuffer writeBuffer;
	ByteBuffer readBuffer;
	long nWritten;
	long nRead;
	public OutputStreamSender(ChannelProcessorMultiplexer multiplexer, int bufferSize) {
		super(multiplexer);
		writeBuffer=ByteBuffer.allocateDirect(bufferSize).order(ChannelProcessorMultiplexer.order);
		readBuffer=writeBuffer.asReadOnlyBuffer().order(ChannelProcessorMultiplexer.order);
		os=new SendOutputStream();
		register();
	}

	public void checkTurnaround(int n) {
		boolean needNotify=false;
		if(nWritten==nRead)
		{
			// We have to mark we have available data
			needNotify=true;
		}
		nWritten+=n;
		if(writeBuffer.position()==writeBuffer.capacity())
		{
			writeBuffer.clear();
			writeBuffer.limit(0);
		}
		if(needNotify)
		{
			dataAvailable();
		}
	}

	@Override
	public int send(SelectionKey key, WritableByteChannel channel, int sendCurrentLength) throws IOException {
		synchronized (writeBuffer) {
			int navail=(int)(nWritten-nRead);
			int l=Math.min(navail, readBuffer.capacity()-readBuffer.position());
			readBuffer.limit(readBuffer.position()+l);
			int n=channel.write(readBuffer);
			if(n>0)
			{
				nRead+=n;
				if(navail==readBuffer.capacity())
				{
					// If buffer was full before but no longer then notify possibly waiting writers.
					writeBuffer.notifyAll();
				}
			}
			return n;
		}
	}

	@Override
	public int getAvailable() {
		synchronized (writeBuffer) {
			return (int)(nWritten-nRead);
		}
	}

}
