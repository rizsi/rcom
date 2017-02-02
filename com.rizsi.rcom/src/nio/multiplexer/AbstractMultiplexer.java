package nio.multiplexer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilFile;

/**
 * A multiplexer implementation that allows several data streams to be sent over a single
 * connection.
 * @author rizsi
 *
 */
abstract public class AbstractMultiplexer implements IMultiplexer
{
	public static final ByteOrder order=ByteOrder.LITTLE_ENDIAN;
	/**
	 * The channel is receiving channel initialization data.
	 */
	private static final int STATE_INIT=0;
	/**
	 * The channel is receiving message header.
	 */
	private static final int STATE_READY=1;
	/**
	 * The channel is receiving a message data.
	 */
	private static final int STATE_MESSAGE=2;
	/**
	 * The channel is receiving user name.
	 */
	private static final int STATE_USERNAME=3;
	/**
	 * Lenght of the authenticated user name in bytes.
	 */
	public static final int userNameLength=64;
	int recvState=STATE_INIT;
	int sendState=STATE_INIT;
	private Map<Integer, MultiplexerReceiver> inputs=new HashMap<>();
	private Map<Integer, MultiplexerSender> outputs=new HashMap<>();
	private MultiplexerReceiver currentInput;
	private int recvCurrentLength;
	private int sendCurrentLength;
	private ByteBuffer recvBuffer=ByteBuffer.allocateDirect(UtilFile.defaultBufferSize.get()).order(order);
	private ByteBuffer sendBuffer=ByteBuffer.allocateDirect(UtilFile.defaultBufferSize.get()).order(order);
	private byte[] thisId;
	private byte[] remoteId;
	private MultiplexerSender currentSender;
	private int nextSenderId;
	private String userName;
	private UtilEvent<Exception> closedEvent= new UtilEvent<>();
	/**
	 * 
	 * @param t
	 * @param c
	 * @param client
	 * @param thisId Identifier of this multiplexer endpoint. This is sent to the client on connection.
	 * @param remoteId Required identifier of the other endpoint. This is checked to be equal to the value received from the client.
	 */
	public AbstractMultiplexer(byte[] thisId, byte[] remoteId) {
		this.thisId=thisId;
		this.remoteId=remoteId;
		sendBuffer.limit(0);
		recvBuffer.limit(0);
	}

	public void write(WritableByteChannel c) throws IOException {
		switch (sendState)
		{
		case STATE_INIT:
		{
			if(!sendBuffer.hasRemaining())
			{
				sendBuffer.clear();
				sendBuffer.put(thisId);
				// Command 0 means start processing multiplexer streams.
				sendBuffer.putInt(0);
				sendBuffer.flip();
			}
			c.write(sendBuffer);
			if(!sendBuffer.hasRemaining())
			{
				sendBuffer.position(0).limit(0);
				sendState=STATE_READY;
			}
			break;
		}
		case STATE_READY:
			if(!sendBuffer.hasRemaining())
			{
				synchronized (this) {
					loop:
					for(MultiplexerSender sender: outputs.values())
					{
						int avail=sender.getAvailable();
						if(avail>0)
						{
							currentSender=sender;
							sendBuffer.clear();
							sendBuffer.putInt(currentSender.getId());
							sendCurrentLength=avail;
							sendBuffer.putInt(sendCurrentLength);
							sendBuffer.flip();
							break loop;
						}
					}
				}
				if(currentSender==null)
				{
					// No sender has data
					signalHasDataToWrite(false);
					return;
				}
			}
			c.write(sendBuffer);
			if(!sendBuffer.hasRemaining())
			{
				sendBuffer.position(0).limit(0);
				sendState=STATE_MESSAGE;
			}
			break;
		case STATE_MESSAGE:
		{
			int n;
			if(currentSender!=null)
			{
				n=currentSender.send(c, sendCurrentLength);
				if(n==0)
				{
					currentSender.close(null);
					currentSender=null;
				}
			}else
			{
				sendBuffer.clear();
				int l=Math.min(sendBuffer.capacity(), sendCurrentLength);
				fillZero(sendBuffer, l);
				sendBuffer.flip();
				n=c.write(sendBuffer);
			}
			sendCurrentLength-=n;
			if(sendCurrentLength<0)
			{
				throw new IOException("Internal error");
			}
			if(sendCurrentLength==0)
			{
				sendBuffer.position(0).limit(0);
				sendState=STATE_READY;
				currentSender=null;
			}
			break;
		}
		default:
			throw new IOException("Internal error");
		}
	}

	private void fillZero(ByteBuffer b, int l) {
		int l8=l/8;
		int lm8=l%8;
		for(int i=0;i<l8;++i)
		{
			b.putLong(0);
		}
		for(int i=0;i<lm8;++i)
		{
			b.put((byte)0);
		}
	}

	public void read(ReadableByteChannel bc) throws IOException {
		switch (recvState)
		{
		case STATE_INIT:
		{
			if(!recvBuffer.hasRemaining())
			{
				recvBuffer.clear();
				recvBuffer.limit(remoteId.length+4);
			}
			int n=bc.read(recvBuffer);
			if(n<0)
			{
				throw new EOFException();
			}
			if(!recvBuffer.hasRemaining())
			{
				byte[] data=new byte[remoteId.length];
				recvBuffer.flip();
				recvBuffer.get(data);
				if(!Arrays.equals(data, remoteId))
				{
					throw new IOException("Invalid client identifier: "+new String(data, StandardCharsets.UTF_8));
				}
				byte command=recvBuffer.get();
				recvBuffer.position(0).limit(0);
				switch(command)
				{
				case 'u':
					if(userName!=null)
					{
						throw new IOException("Username already set up");
					}
					recvState=STATE_USERNAME;
					break;
				case 0:
					recvState=STATE_READY;
					break;
				default:
					throw new IOException("invalid command: "+command);
				}
			}
			break;
		}
		case STATE_READY:
		{
			if(!recvBuffer.hasRemaining())
			{
				recvBuffer.limit(8);
			}
			int n=bc.read(recvBuffer);
			if(n<0)
			{
				throw new EOFException();
			}
			if(recvBuffer.remaining()==0)
			{
				recvBuffer.flip();
				int channel=recvBuffer.getInt();
				recvCurrentLength=recvBuffer.getInt();
				currentInput=inputs.get(channel);
				recvState=STATE_MESSAGE;
			}
			break;
		}
		case STATE_MESSAGE:
		{
			if(currentInput==null)
			{
				int n=Math.min(recvBuffer.capacity(), recvCurrentLength);
				recvBuffer.clear();
				recvBuffer.limit(n);
				n=bc.read(recvBuffer);
				if(n<0)
				{
					throw new EOFException();
				}
				recvCurrentLength-=n;
			}else
			{
				int n=currentInput.read(bc, recvCurrentLength);
				if(n<0)
				{
					throw new EOFException();
				}
				recvCurrentLength-=n;
			}
			if(recvCurrentLength<0)
			{
				throw new IOException("Internal error");
			}
			if(recvCurrentLength==0)
			{
				recvBuffer.position(0).limit(0);
				recvState=STATE_READY;
			}
			break;
		}
		case STATE_USERNAME:
			if(!recvBuffer.hasRemaining())
			{
				recvBuffer.position(0).limit(userNameLength);
			}
			int n=bc.read(recvBuffer);
			if(n<0)
			{
				throw new EOFException();
			}
			if(recvBuffer.remaining()==0)
			{
				recvBuffer.flip();
				for_:
				for(int i=0;i<recvBuffer.limit();++i)
				{
					if(recvBuffer.get(i)==0)
					{
						byte[] data=new byte[i];
						recvBuffer.get(data);
						userName=new String(data, StandardCharsets.UTF_8);
						break for_;
					}
				}
				recvState=STATE_INIT;
				recvBuffer.position(0).limit(0);
			}
			break;
		default:
			throw new IOException("Internal error");
		}
	}

	public void register(MultiplexerReceiver multiplexerReceiver, int id) {
		synchronized (this) {
			inputs.put(id, multiplexerReceiver);
		}
		
	}
	public void register(MultiplexerSender multiplexerSender) {
		synchronized (this) {
			int id=nextSenderId++;
			multiplexerSender.setId(id);
			outputs.put(id, multiplexerSender);
		}
		dataAvailable(multiplexerSender);
	}

	public void remove(MultiplexerSender multiplexerSender) {
		synchronized (this) {
			outputs.remove(multiplexerSender.getId());
		}
	}

	public void dataAvailable(MultiplexerSender multiplexerSender) {
		signalHasDataToWrite(true);
	}

	public void remove(MultiplexerReceiver multiplexerReceiver) {
		synchronized (this) {
			inputs.remove(multiplexerReceiver.getId());
		}
	}

	public void closeMultiplexer(Exception e) {
		List<MultiplexerReceiver> toClose;
		List<MultiplexerSender> toCloseS;
		synchronized (this) {
			toClose=new ArrayList<MultiplexerReceiver>(inputs.values());
			inputs.clear();
			toCloseS=new ArrayList<>(outputs.values());
			outputs.clear();
		}
		for(MultiplexerReceiver r:toClose)
		{
			r.close(e);
		}
		for(MultiplexerSender r:toCloseS)
		{
			r.close(e);
		}
		closed(e);
		getClosedEvent().eventHappened(e);
	}

	
	abstract protected void closed(Exception e);

	abstract protected void signalHasDataToWrite(boolean b);
	
	@Override
	public String getUserName() {
		return userName;
	}
	@Override
	public UtilEvent<Exception> getClosedEvent() {
		return closedEvent;
	}
}
