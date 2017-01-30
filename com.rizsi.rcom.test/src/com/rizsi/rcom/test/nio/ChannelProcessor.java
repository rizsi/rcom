package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;


/**
 * A single instance of this object must be connected to each channel used in the selector.
 * 
 * The method "register" has to be called once to register this channel with the hosting {@link NioThread}.
 * 
 * @author rizsi
 *
 */
abstract public class ChannelProcessor {
	final public NioThread t;
	final public SelectableChannel c;
	private volatile SelectionKey key;
	public ChannelProcessor(NioThread t, SelectableChannel c) {
		super();
		this.t = t;
		this.c = c;
	}
	/**
	 * This method has to be called once before using the channel.
	 * @param hasData
	 * @throws ClosedChannelException 
	 */
	public void register(int interestOps) throws ClosedChannelException
	{
		key=c.register(t.s, interestOps, this);
	}
	/**
	 * Update the has data to write state of the channel.
	 * Must only be called after registration is done (register() method).
	 * @param hasDataToWrite
	 */
	public void setHasDataToWrite(boolean hasDataToWrite)
	{
		if(hasDataToWrite)
		{
			key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
		}else
		{
			key.interestOps(key.interestOps()&~SelectionKey.OP_WRITE);
		}
	}
	abstract public void accept(SelectionKey key);
	/**
	 * Handle key is invalid. The {@link NioThread} will cancel the key. All other dispose has to be done here.
	 * @param key
	 */
	abstract public void keyInvalid(SelectionKey key);
	abstract public void write(SelectionKey key);
	abstract public void read(SelectionKey key) throws IOException;
	abstract public void connect(SelectionKey key);
}
