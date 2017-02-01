package com.rizsi.rcom;

import java.io.IOException;

import nio.multiplexer.ChannelProcessorMultiplexer;
import nio.multiplexer.InputStreamReceiver;

abstract public class StreamSinkSimplex extends StreamSink
{
	private String name;
	protected InputStreamReceiver receiver;
	public StreamSinkSimplex(String name) {
		this.name=name;
	}
	abstract public void start() throws IOException, Exception;
	@Override
	public void start(AbstractRcomArgs args, IVideocomConnection conn, ChannelProcessorMultiplexer multiplexer) throws Exception {
		receiver=new InputStreamReceiver(VideoConnection.bufferSize);
		start();
		IStreamData data=conn.registerStream(name, -1);
		StreamDataSimplex d=(StreamDataSimplex) data;
		receiver.register(multiplexer, d.channel);
		conn.launchStream(name);
	}
	public void dispose()
	{
		receiver.close(null);
	}
}
