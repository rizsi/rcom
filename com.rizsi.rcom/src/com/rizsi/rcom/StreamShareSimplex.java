package com.rizsi.rcom;

import java.util.ArrayList;
import java.util.List;

import nio.multiplexer.MultiplexerSender;

public class StreamShareSimplex extends StreamShare {
	class Reg implements StreamRegistration
	{
		MultiplexerSender cos;
		
		public Reg(MultiplexerSender cos) {
			super();
			this.cos = cos;
		}

		@Override
		public void close() {
			cos.close(null);
		}

		@Override
		public IStreamData getData() {
			return new StreamDataSimplex(cos.getId());
		}

		@Override
		public void launch() {
			// Simplex streams are launched at once without waiting for the client to actually listen to the channel.
		}
	}
	private RoundBufferStreaming buffer;
	private List<Reg> clients=new ArrayList<>();
	private int channel;
	public StreamShareSimplex(VideoConnection videoConnection, int channel, StreamParameters params) {
		super(videoConnection, params);
		this.channel=channel;
		buffer=new RoundBufferStreaming(VideoConnection.bufferSize);
		buffer.connectReceiver(videoConnection.getConnection(), channel);
	}

	public void dispose() {
		for(Reg r: clients)
		{
			r.close();
		}
		clients.clear();
	}

	@Override
	public StreamRegistration registerClient(VideoConnection videoConnection, int channel) {
		if(channel!=-1)
		{
			throw new IllegalArgumentException();
		}
		MultiplexerSender sender=buffer.createSender(videoConnection.getConnection());
		Reg ret=new Reg(sender);
		clients.add(ret);
		return ret;
	}

	@Override
	public IStreamData getStreamData() {
		return new StreamDataSimplex(channel);
	}

}
