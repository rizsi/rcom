package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChannelProcessorMultiplexer extends ChannelProcessor
{
	private static final int STATE_INIT=0;
	private static final int STATE_READY=1;
	private static final int STATE_MESSAGE=2;
	int recvState=STATE_INIT;
	int sendState=STATE_INIT;
	private Map<Integer, MultiplexerReceiver> inputs;
	private Map<Integer, MultiplexerSender> outputs;
	private MultiplexerReceiver currentInput;
	private int nCanWrite;
	private boolean hasData;
	public ChannelProcessorMultiplexer(NioThread t, SelectableChannel c) {
		super(t, c);
		if(!(c instanceof ByteChannel))
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void accept(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyInvalid(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}
	private MultiplexerSender currentSender;
	@Override
	public void write(SelectionKey key) {
		switch (recvState)
		{
		case STATE_INIT:
			// TODO read 4 bytes then find the currentInput
			// If currentInput is null then use dummyCurrentInput which streams data to /dev/null
			break;
		case STATE_READY:
			synchronized (this) {
				for(MultiplexerSender sender: outputs.values())
				{
					if(sender.canWrite())
					{
						currentSender=sender;
					}
				}
			}
			// TODO send n bytes header
		case STATE_MESSAGE:
			currentSender.send(key, (WritableByteChannel)key.channel());
			break;
		default:
			// TODO error - close stream
		}
	}

	@Override
	public void read(SelectionKey key) throws IOException {
		switch (recvState)
		{
		case STATE_INIT:
			// TODO read 4 bytes then find the currentInput
			// If currentInput is null then use dummyCurrentInput which streams data to /dev/null
			break;
		case STATE_READY:
			ReadableByteChannel bc=(ReadableByteChannel)key.channel();
			currentInput.read(key, bc);
		case STATE_MESSAGE:
		default:
			// TODO error - close stream
		}
	}

	@Override
	public void connect(SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is called when synchronized to this.
	 * @param multiplexerSender
	 * @param b
	 */
	protected void canWriteChanged(MultiplexerSender multiplexerSender, boolean b) {
		nCanWrite+=b?1:-1;
		boolean hasData=nCanWrite>0;
		if(this.hasData!=hasData)
		{
			this.hasData=hasData;
			setHasDataToWrite(hasData);
		}
	}

}
