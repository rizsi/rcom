package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

/**
 * Receiver endpoint of the multiplexer.
 * 
 * The receiver must process all the input received and must not block the thread in the read method.
 * 
 * @author rizsi
 *
 */
abstract public class MultiplexerReceiver {
	ChannelProcessorMultiplexer multiplexer;
	private int id;
	public void register(ChannelProcessorMultiplexer multiplexer, int id)
	{
		this.multiplexer=multiplexer;
		multiplexer.register(this, id);
		this.id=id;
	}
	public int getId() {
		return id;
	}
	/**
	 * Read from the channel. Called when at least 1 byte is available.
	 * 
	 * Must not block the calling thread (or it will block the whole NIO server thread).
	 * 
	 * @param key 
	 * @param bc
	 * @param remainingBytes maximum bytes to read
	 * @return number of bytes read. Must not be 0 because that would result in busy loop in the server thread. -1 means EOF.
	 * @throws IOException
	 */
	abstract public int read(SelectionKey key, ReadableByteChannel bc, int remainingBytes) throws IOException;
	public void close(Exception e)
	{
		if(multiplexer!=null)
		{
			multiplexer.remove(this);
		}
	}
}
