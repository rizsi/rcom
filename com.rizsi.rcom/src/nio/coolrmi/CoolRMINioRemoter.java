package nio.coolrmi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import hu.qgears.commons.UtilFile;
import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;
import hu.qgears.coolrmi.multiplexer.ISocketMultiplexer;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;
import nio.NioThread;
import nio.multiplexer.ChannelProcessorMultiplexer;
import nio.multiplexer.MultiplexerReceiver;
import nio.multiplexer.MultiplexerSender;

public class CoolRMINioRemoter extends GenericCoolRMIRemoter {
	private int maxMessageSize=UtilFile.defaultBufferSize.get()*512;
	private ByteBuffer recvBuffer=ByteBuffer.allocateDirect(maxMessageSize);
	public static final byte[] serverId="COOLRMI SERVER V0.0.0".getBytes(StandardCharsets.UTF_8);
	public static final byte[] clientId="COOLRMI CLIENT V0.0.0".getBytes(StandardCharsets.UTF_8);

	private boolean exit;
	ConcurrentLinkedQueue<Msg> toSend=new ConcurrentLinkedQueue<>();
	private Send s;
	private LinkedBlockingQueue<byte[]> toProcess=new LinkedBlockingQueue<>();
	private boolean server;
	class Msg
	{
		byte[] bs;
		AbstractCoolRMIMessage message;
		ByteBuffer toSend;
		public Msg(byte[] bs, AbstractCoolRMIMessage message) {
			super();
			this.bs = bs;
			this.message = message;
			toSend=ByteBuffer.wrap(bs);
		}
		
	}
	
	class Mpx implements ISocketMultiplexer
	{
		@Override
		public void addMessageToSend(byte[] bs, AbstractCoolRMIMessage message) {
			toSend.add(new Msg(bs, message));
			s.dataAvailable();
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}
		
	}
	class Recv extends MultiplexerReceiver
	{
		public Recv() {
		}

		@Override
		public int read(SelectionKey key, ReadableByteChannel bc, int remainingBytes) throws IOException {
			recvBuffer.limit(recvBuffer.position()+remainingBytes);
			int n=bc.read(recvBuffer);
			if(n==remainingBytes)
			{
				byte[] msg=new byte[recvBuffer.position()];
				recvBuffer.flip();
				recvBuffer.get(msg);
				toProcess.add(msg);
				recvBuffer.clear();
			}
			return n;
		}
		@Override
		public void close(Exception e) {
			super.close(e);
			pipeBroken(e);
		}
		
	}
	class Send extends MultiplexerSender
	{
		public Send(ChannelProcessorMultiplexer multiplexer) {
			super(multiplexer);
		}

		Msg current;
		private void updatecurrent()
		{
			if(current==null)
			{
				current=toSend.poll();
			}
		}
		@Override
		public int send(SelectionKey key, WritableByteChannel channel, int sendCurrentLength) throws IOException {
			updatecurrent();
			if(current==null)
			{
				return 0;
			}
			int n=channel.write(current.toSend);
			if(!current.toSend.hasRemaining())
			{
				current.message.sent();
				current=null;
			}
			return n;
		}

		@Override
		public int getAvailable() {
			updatecurrent();
			if(current!=null)
			{
				return current.toSend.remaining();
			}
			return 0;
		}
		@Override
		public void close(Exception e) {
			super.close(e);
			try {
				CoolRMINioRemoter.this.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}

	public CoolRMINioRemoter(ClassLoader classLoader, boolean guaranteeOrdering, boolean server) {
		super(classLoader, guaranteeOrdering);
		this.server=server;
	}

	@Override
	protected void closeConnection() throws IOException {
		exit=true;
		// Notify the message processing thread
		toProcess.add(new byte[]{});
	}

	public void connect(NioThread t, SocketChannel sc) throws ClosedChannelException, InterruptedException, ExecutionException {
		ChannelProcessorMultiplexer m=new ChannelProcessorMultiplexer(t, sc, true,
				server?serverId:clientId, server?clientId:serverId);
		s=new Send(m);
		s.register();
		Recv r=new Recv();
		r.register(m, 0);
		multiplexer=new Mpx();
		m.start();
		new Thread("CoolRMI client thread")
		{
			public void run() {
				try {
					while(!exit)
					{
						byte[] msg=toProcess.take();
						if(!exit)
						{
							messageReceived(msg);
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}
		.start();
	}

	@Override
	public void execute(Runnable runnable) {
		runnable.run();
	}

}
