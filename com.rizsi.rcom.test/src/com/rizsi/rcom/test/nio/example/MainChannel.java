package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import nio.multiplexer.ChannelProcessorMultiplexer;
import nio.multiplexer.MultiplexerReceiver;
import nio.multiplexer.MultiplexerSender;

public class MainChannel {
	class Send extends MultiplexerSender
	{
		byte[] data="Helloka!".getBytes();
		ByteBuffer bb=ByteBuffer.wrap(data);
		public Send(ChannelProcessorMultiplexer multiplexer) {
			super(multiplexer);
			register();
		}

		@Override
		public int send(SelectionKey key, WritableByteChannel channel, int sendCurrentLength) throws IOException {
			int ret=channel.write(bb);
			return ret;
		}

		@Override
		public int getAvailable() {
			return bb.remaining();
		}
	}
	class Receive extends MultiplexerReceiver
	{
		@Override
		public int read(SelectionKey key, ReadableByteChannel bc, int remainingBytes) throws IOException {
			byte[] data=new byte[remainingBytes];
			ByteBuffer dst=ByteBuffer.wrap(data);
			int n=bc.read(dst);
			System.out.println("Recv: "+new String(data, 0, n));
			return n;
		}
	}
	Send send;
	Receive recv;
	public void register(ChannelProcessorMultiplexer m) {
		send=new Send(m);
		recv=new Receive();
		recv.register(m, 0);
	}
}
