package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

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
	 * Read from the buffer
	 * @param key 
	 * @param bc
	 * @param remainingBytes maximum bytes to read
	 * @return number of bytes read. Must not be 0 because blocking is not possible and data is available. -1 means EOF.
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
