package nio.multiplexer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;

import nio.ChannelProcessor;
import nio.NioThread;

/**
 * A multiplexer implementation that allows several data streams to be sent over a single
 * TCP conenction.
 * @author rizsi
 *
 */
public class ChannelProcessorMultiplexer extends AbstractMultiplexer
{
	private CP cp;
	private class CP extends ChannelProcessor
	{
		public CP(NioThread t, SelectableChannel c, boolean clientSocket) {
			super(t, c, clientSocket);
		}
		@Override
		public void register(int interestOps) throws ClosedChannelException {
			super.register(interestOps);
		}
		@Override
		public void accept(SelectionKey key) throws IOException {
			throw new IOException("Accept is not possible on this object.");
		}

		@Override
		public void keyInvalid(SelectionKey key) {
		}
		@Override
		public void write(SelectionKey key) throws IOException {
			ChannelProcessorMultiplexer.this.write((WritableByteChannel)key.channel());
		}
		@Override
		public void read(SelectionKey key) throws IOException {
			ChannelProcessorMultiplexer.this.read((ReadableByteChannel)key.channel());
		}
		@Override
		public void close(Exception e) {
			ChannelProcessorMultiplexer.this.closeMultiplexer(e);
		}
	}
	/**
	 * 
	 * @param t
	 * @param c
	 * @param client
	 * @param thisId Identifier of this multiplexer endpoint. This is sent to the client on connection.
	 * @param remoteId Required identifier of the other endpoint. This is checked to be equal to the value received from the client.
	 */
	public ChannelProcessorMultiplexer(NioThread t, SelectableChannel c, boolean client, byte[] thisId, byte[] remoteId) {
		super(thisId, remoteId);
		cp=new CP(t, c, client);
	}
	
	public void start() throws ClosedChannelException, InterruptedException, ExecutionException
	{
		cp.register(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
	}
	@Override
	protected void closed(Exception e) {
		if(e!=null)
		{
			System.err.println("Channel closed:");
			e.printStackTrace();
		}
	}
	@Override
	protected void signalHasDataToWrite(boolean b) {
		cp.setHasDataToWrite(b);
	}
}
