package com.rizsi.rcom.test.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

public class MultiplexerSender {
	private final ChannelProcessorMultiplexer multiplexer;
	
	private boolean canWrite;
	
	public MultiplexerSender(ChannelProcessorMultiplexer multiplexer) {
		super();
		this.multiplexer = multiplexer;
	}
	/**
	 * 
	 * @param n number of bytes available to send.
	 */
	public void setCanWrite(boolean b)
	{
		synchronized (multiplexer) {
			if(b!=canWrite)
			{
				multiplexer.canWriteChanged(this, b);
			}
			canWrite=b;
		}
	}
	boolean canWrite()
	{
		return canWrite;
	}
	public void send(SelectionKey key, WritableByteChannel channel) {
		// TODO Auto-generated method stub
		
	}

}
