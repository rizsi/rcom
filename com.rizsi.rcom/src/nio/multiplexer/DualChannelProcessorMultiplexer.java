package nio.multiplexer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.concurrent.ExecutionException;

import nio.ChannelProcessor;
import nio.NioThread;

/**
 * A multiplexer implementation that allows several data streams to be sent over a readable and a writable
 * connection
 * @author rizsi
 *
 */
public class DualChannelProcessorMultiplexer extends AbstractMultiplexer
{
	private CPSink cpSink;
	private CPSource cpSource;
	private class CPSink extends ChannelProcessor
	{
		public CPSink(NioThread t, SelectableChannel c, boolean clientSocket) {
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
			DualChannelProcessorMultiplexer.this.write((WritableByteChannel)key.channel());
		}
		@Override
		public void read(SelectionKey key) throws IOException {
			throw new IOException("Read is not possible on this object.");
		}
		@Override
		public void close(Exception e) {
			try {
				c.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			DualChannelProcessorMultiplexer.this.closeMultiplexer(e);
		}
	}
	private class CPSource extends ChannelProcessor
	{
		public CPSource(NioThread t, SelectableChannel c, boolean clientSocket) {
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
			throw new IOException("Write is not possible on this object.");
		}
		@Override
		public void read(SelectionKey key) throws IOException {
			DualChannelProcessorMultiplexer.this.read((ReadableByteChannel)key.channel());
		}
		@Override
		public void close(Exception e) {
			DualChannelProcessorMultiplexer.this.closeMultiplexer(e);
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
	public DualChannelProcessorMultiplexer(NioThread t, SourceChannel source, SinkChannel sink, boolean client, byte[] thisId, byte[] remoteId) {
		super(thisId, remoteId);
		cpSink=new CPSink(t, sink, client);
		cpSource=new CPSource(t, source, client);
	}
	
	public void start() throws ClosedChannelException, InterruptedException, ExecutionException
	{
		cpSink.register(SelectionKey.OP_WRITE);
		cpSource.register(SelectionKey.OP_READ);
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
		cpSink.setHasDataToWrite(b);
	}
}
