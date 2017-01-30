package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

abstract public class MultiplexerSender {
	private final ChannelProcessorMultiplexer multiplexer;
	
	private boolean canWrite;
	private int id;
	private boolean hasData;
	protected void setHasData(boolean hasData) {
		this.hasData = hasData;
	}
	protected boolean getHasData(boolean hasData) {
		return this.hasData;
	}
	
	public MultiplexerSender(ChannelProcessorMultiplexer multiplexer) {
		super();
		this.multiplexer = multiplexer;
	}
	public void register()
	{
		multiplexer.register(this);
	}
	/**
	 * Notify the NIO thread that the can write state has to be updated.
	 */
	public void dataAvailable()
	{
		multiplexer.dataAvailable(this);
	}
	boolean canWrite()
	{
		return canWrite;
	}
	/**
	 * 
	 * @param key
	 * @param channel
	 * @param sendCurrentLength
	 * @return number of bytes written to the target Must be positive. 0 means error and this sender is going to be closed.
	 */
	abstract public int send(SelectionKey key, WritableByteChannel channel, int sendCurrentLength) throws IOException;
	public int getId() {
		return id;
	}
	abstract public int getAvailable();
	final protected void setId(int id) {
		this.id=id;
	}
	public void close(Exception e)
	{
		multiplexer.remove(this);
	}
}
