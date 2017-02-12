package nio.multiplexer;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Receiver endpoint of the multiplexer.
 * 
 * The receiver must process all the input received and must not block the thread in the read method.
 * 
 * @author rizsi
 *
 */
abstract public class MultiplexerReceiver {
	IMultiplexer multiplexer;
	private int id;
	volatile private long receiveBufferAvailavle=-1;
	public void register(IMultiplexer multiplexer, int id)
	{
		this.multiplexer=multiplexer;
		multiplexer.register(this, id);
		this.id=id;
		if(receiveBufferAvailavle!=-1)
		{
			multiplexer.availableChanged(this);
		}
	}
	public int getId() {
		return id;
	}
	/**
	 * Read from the channel. Called when at least 1 byte is available.
	 * 
	 * Must not block the calling thread (or it will block the whole NIO server thread).
	 * 
	 * @param bc
	 * @param remainingBytes maximum bytes to read
	 * @return number of bytes read. Must not be 0 because that would result in busy loop in the server thread. -1 means EOF.
	 * @throws IOException
	 */
	abstract public int read(ReadableByteChannel bc, int remainingBytes) throws IOException;
	
	/**
	 * Get the currently available receive buffer size. This size is sent to the client in case it is not -1.
	 * @return -1 means the buffer can receive any number of bytes. (eg. Asynchronous streaming)
	 */
	public long getReceiveBufferAvailable()
	{
		return receiveBufferAvailavle;
	}
	/**
	 * The number of bytes in the receive buffer counted from the start of the stream.
	 * 
	 * @param avail counted from the start of the stream because sending and receiving is asynchronous so sending the current available receive buffer would have no meaning.
	 */
	public void setReceiveBufferAvailable(long avail)
	{
		if(avail!=receiveBufferAvailavle)
		{
			receiveBufferAvailavle=avail;
			if(multiplexer!=null)
			{
				multiplexer.availableChanged(this);
			}
		}
	}
	public void close(Exception e)
	{
		if(multiplexer!=null)
		{
			multiplexer.remove(this);
		}
	}
}
