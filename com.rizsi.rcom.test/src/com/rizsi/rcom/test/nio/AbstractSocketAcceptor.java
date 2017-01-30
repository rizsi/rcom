package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

abstract public class AbstractSocketAcceptor extends ChannelProcessor{
	private ServerSocketChannel c;

	public AbstractSocketAcceptor(NioThread t, ServerSocketChannel c) {
		super(t, c, false);
		this.c=c;
	}
	@Override
	final public void accept(SelectionKey key) throws IOException {
		SocketChannel sc=c.accept();
		socketChannelAccepted(sc);
	}

	abstract protected void socketChannelAccepted(SocketChannel sc) throws IOException;
	@Override
	public void keyInvalid(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(SelectionKey key) throws IOException {
		throw new IOException("Not allowed.");
	}

	@Override
	public void read(SelectionKey key) throws IOException {
		throw new IOException("Not allowed.");
	}

	public void start() throws ClosedChannelException, InterruptedException, ExecutionException {
		register(SelectionKey.OP_ACCEPT);
	}

}
