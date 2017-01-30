package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;
import com.rizsi.rcom.test.nio.MultiplexerReceiver;
import com.rizsi.rcom.test.nio.MultiplexerSender;

public class RoundBuffer {
	private ByteBuffer buffer;
	private long nRead;
	private List<Send> senders=new ArrayList<>();
	private class Receive extends MultiplexerReceiver{
		@Override
		public int read(SelectionKey key, ReadableByteChannel bc, int remainingBytes) throws IOException {
			int n=Math.min(remainingBytes, buffer.capacity()-buffer.position());
			List<Send> toactivate=null;
			synchronized (buffer) {
				buffer.limit(buffer.position()+n);
				n=bc.read(buffer);
				if(buffer.position()==buffer.capacity())
				{
					// Buffer is filled. We restart on the other end.
					buffer.position(0);
				}
				if(n>0)
				{
					nRead+=n;
					toactivate=RoundBuffer.this.senders;
				}
			}
			if(toactivate!=null)
			{
				for(Send s: toactivate)
				{
					s.dataAvailable();
				}
			}
			return n;
		}
	}
	
	private class Send extends MultiplexerSender
	{
		private long nSent;
		private ByteBuffer sendBuffer;
		private boolean overflow;
		public Send(ChannelProcessorMultiplexer multiplexer) {
			super(multiplexer);
			synchronized (buffer) {
				sendBuffer=buffer.asReadOnlyBuffer().order(buffer.order());
				// As if all already received data was already sent to the client.
				// Only new data is passed
				nSent=nRead;
			}
			sendBuffer.position((int)(nSent%sendBuffer.capacity()));
			register();
		}

		@Override
		public int send(SelectionKey key, WritableByteChannel channel, int sendCurrentLength) throws IOException {
			// The minimum of buffer, required package size and data available
			int n;
			synchronized (buffer) {
				long lag=nRead-nSent;
				if(lag>sendBuffer.capacity())
				{
					overflow=true;
					// Overflow error, close this sender. 
					n=0;
				}else
				{
					n=Math.min(sendCurrentLength, sendBuffer.capacity()-sendBuffer.position());
					n=Math.min(n, getAvailable());
					sendBuffer.limit(sendBuffer.position()+n);
					n=channel.write(sendBuffer);
				}
			}
			if(n>0)
			{
				nSent+=n;
			}
			if(sendBuffer.position()==sendBuffer.capacity())
			{
				// We have reached the end of the send buffer. Next time start with the beginning.
				sendBuffer.clear();
			}
			return n;
		}

		@Override
		public int getAvailable() {
			synchronized (buffer) {
				return (int)(nRead-nSent);
			}
		}
		@Override
		public void close(Exception e) {
			synchronized (buffer) {
				senders=new ArrayList<>(senders);
				senders.remove(this);
			}
			super.close(e);
		}
	}
	public RoundBuffer(int capacity)
	{
		buffer=ByteBuffer.allocateDirect(capacity).order(ChannelProcessorMultiplexer.order);
	}
	public MultiplexerSender createSender(ChannelProcessorMultiplexer multiplexer)
	{
		Send s=new Send(multiplexer);
		synchronized (buffer) {
			senders=new ArrayList<>(senders);
			senders.add(s);
		}
		return s;
	}
	public Receive connectReceiver(ChannelProcessorMultiplexer multiplexer)
	{
		Receive r=new Receive();
		return r;
	}
}
