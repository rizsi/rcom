package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * Base class that can be used as a handler of a server socket in a {@link NioThread}
 * based NIO server to handle incoming connection.
 * @author rizsi
 *
 */
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
	/**
	 * Handle the incoming connection.
	 * Must not block the calling thread as it is the {@link NioThread}.
	 * 
	 * A typical implementation will call sc.configureBlocking(false) and then
	 * initialize and register a {@link ChannelProcessor} object for the connection.
	 * 
	 * @param sc
	 * @throws IOException
	 */
	abstract protected void socketChannelAccepted(SocketChannel sc) throws IOException;
	@Override
	public void keyInvalid(SelectionKey key) {
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
