package com.rizsi.rcom.test.nio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hu.qgears.commons.UtilFile;

public class ChannelProcessorMultiplexer extends ChannelProcessor
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
	 * The channel is rceiving a message data.
	 */
	private static final int STATE_MESSAGE=2;
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
	public ChannelProcessorMultiplexer(NioThread t, SelectableChannel c, boolean client, byte[] thisId, byte[] remoteId) {
		super(t, c, client);
		this.thisId=thisId;
		this.remoteId=remoteId;
		if(!(c instanceof ByteChannel))
		{
			throw new IllegalArgumentException();
		}
		sendBuffer.limit(0);
		recvBuffer.limit(0);
	}
	
	public void start() throws ClosedChannelException, InterruptedException, ExecutionException
	{
		register(SelectionKey.OP_WRITE|SelectionKey.OP_READ);
	}

	@Override
	public void accept(SelectionKey key) throws IOException {
		throw new IOException("Accept is not possible on this object.");
	}

	@Override
	public void keyInvalid(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void write(SelectionKey key) throws IOException {
		WritableByteChannel c=(WritableByteChannel)key.channel();
		switch (sendState)
		{
		case STATE_INIT:
		{
			if(!sendBuffer.hasRemaining())
			{
				sendBuffer.clear();
				sendBuffer.put(thisId);
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
					setHasDataToWrite(false);
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
				n=currentSender.send(key, c, sendCurrentLength);
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

	@Override
	public void read(SelectionKey key) throws IOException {
		ReadableByteChannel bc=(ReadableByteChannel)key.channel();
		switch (recvState)
		{
		case STATE_INIT:
		{
			if(!recvBuffer.hasRemaining())
			{
				recvBuffer.clear();
				recvBuffer.limit(remoteId.length);
			}
			int n=bc.read(recvBuffer);
			if(n<0)
			{
				throw new EOFException();
			}
			if(!recvBuffer.hasRemaining())
			{
				// TODO check header received!
				recvBuffer.position(0).limit(0);
				recvState=STATE_READY;
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
				int n=currentInput.read(key, bc, recvCurrentLength);
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
		default:
			throw new IOException("Internal error");
		}
	}

	public void register(MultiplexerReceiver multiplexerReceiver, int id) {
		synchronized (this) {
			inputs.put(id, multiplexerReceiver);
		}
		
	}
	private int nextSenderId;
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
		setHasDataToWrite(true);
	}

	public void remove(MultiplexerReceiver multiplexerReceiver) {
		synchronized (this) {
			inputs.remove(multiplexerReceiver.getId());
		}
	}

	@Override
	public void close(Exception e) {
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
		System.err.println("Channel closed:");
		e.printStackTrace();
	}

}
