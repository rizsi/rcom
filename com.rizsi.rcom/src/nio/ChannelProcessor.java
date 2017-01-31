package nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;


/**
 * A single instance of this object must be connected to each channel used in the selector.
 * 
 * The method "register" has to be called once to register this channel with the hosting {@link NioThread}.
 * 
 * @author rizsi
 *
 */
abstract public class ChannelProcessor
{
	final public NioThread t;
	final public SelectableChannel c;
	private volatile SelectionKey key;
	protected final boolean clientSocket;
	public ChannelProcessor(NioThread t, SelectableChannel c, boolean clientSocket) {
		super();
		this.t = t;
		this.c = c;
		this.clientSocket=clientSocket;
	}
	/**
	 * This method has to be called once before using the channel.
	 * @param interestOps operations that are going to be handled. OP_Connect need not be set because it is automatically added for client connections.
	 * @throws ClosedChannelException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	protected void register(int interestOps) throws ClosedChannelException
	{
		if(clientSocket)
		{
			interestOps|=SelectionKey.OP_CONNECT;
		}
		if(Thread.currentThread()==t)
		{
			key=t.register(c, interestOps, ChannelProcessor.this);
		}else
		{
			int interestOpsFinal=interestOps;
			t.addTask(new Runnable() {
				
				@Override
				public void run() {
					try {
						key=t.register(c, interestOpsFinal, ChannelProcessor.this);
					} catch (ClosedChannelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		
	}
	/**
	 * Update the has data to write state of the channel.
	 * Must only be called after registration is done (register() method).
	 * @param hasDataToWrite
	 */
	public void setHasDataToWrite(boolean hasDataToWrite)
	{
		if(Thread.currentThread()==t)
		{
			setHasDataToWriteOnThread(hasDataToWrite);
		}else
		{
			t.addTask(new Runnable() {
				@Override
				public void run() {
					setHasDataToWriteOnThread(hasDataToWrite);
				}
			});
		}
	}
	private void setHasDataToWriteOnThread(boolean hasDataToWrite) {
		if(key!=null)
		{	
			if(hasDataToWrite)
			{
				key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
			}else
			{
				key.interestOps(key.interestOps()&~SelectionKey.OP_WRITE);
			}
		}
	}
	abstract public void accept(SelectionKey key) throws IOException;
	/**
	 * Handle key is invalid. The {@link NioThread} will cancel the key. All other dispose has to be done here.
	 * @param key
	 */
	abstract public void keyInvalid(SelectionKey key);
	abstract public void write(SelectionKey key) throws IOException;
	abstract public void read(SelectionKey key) throws IOException;
	final public void connect(SelectionKey key) throws IOException
	{
		if(clientSocket)
		{
			if(!((SocketChannel)key.channel()).finishConnect())
			{
				throw new IOException("Error connecting the channel.");
			}
			key.interestOps(key.interestOps()&~SelectionKey.OP_CONNECT);
			connected(key);
		}else
		{
			throw new IOException("Connect is not allowed on non-client channel.");
		}
	}
	/**
	 * Override to implement functionality on connection.
	 * @param key
	 */
	protected void connected(SelectionKey key) {
		
	}
	/**
	 * Channel is closed. Dispose all associated resources.
	 * @param e 
	 */
	abstract public void close(Exception e);
}
