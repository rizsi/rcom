package nio.multiplexer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
	}
	public final OutputStream os;
	private ByteBuffer writeBuffer;
	private ByteBuffer readBuffer;
	private long nWritten;
	private long nRead;
	private volatile long nRemote;
	private boolean noOverflow;
	public OutputStreamSender(IMultiplexer multiplexer, int bufferSize, boolean noOverflow) {
		super(multiplexer);
		this.noOverflow=noOverflow;
		writeBuffer=ByteBuffer.allocateDirect(bufferSize).order(ChannelProcessorMultiplexer.order);
		readBuffer=writeBuffer.asReadOnlyBuffer().order(ChannelProcessorMultiplexer.order);
		os=new SendOutputStream();
		register();
	}
	@Override
	public int send(WritableByteChannel channel, int sendCurrentLength) throws IOException {
		synchronized (writeBuffer) {
			int navail=(int)(nWritten-nRead);
			int l=Math.min(navail, readBuffer.capacity()-readBuffer.position());
			l=Math.min(sendCurrentLength, l);
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
			if(readBuffer.position()==readBuffer.capacity())
			{
				readBuffer.position(0);
			}
			return n;
		}
	}

	@Override
	public int getAvailable() {
		synchronized (writeBuffer) {
			long n;
			if(noOverflow)
			{
				if(nRemote<0)
				{
					return 0;
				}
				n=Math.min(nWritten, nRemote);
			}else
			{
				n=nWritten;
			}
			return (int)(n-nRead);
		}
	}
	@Override
	public void receiveBufferAvailable(long receiverAvailable) {
		if(nRemote!=receiverAvailable)
		{
			nRemote=receiverAvailable;
			int avail=getAvailable();
			if(avail>0)
			{
				dataAvailable();
			}
		}
	}
}
