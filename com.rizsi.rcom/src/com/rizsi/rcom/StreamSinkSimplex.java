package com.rizsi.rcom;

import java.io.IOException;

abstract public class StreamSinkSimplex extends StreamSink implements IChannelReader
{
	private String name;
	public StreamSinkSimplex(String name) {
		this.name=name;
	}
	abstract public void start() throws IOException, Exception;
	@Override
	public void start(AbstractRcomArgs args, IVideocomConnection conn, ChannelMultiplexer multiplexer) throws Exception {
		start();
		IStreamData data=conn.registerStream(name, -1);
		StreamDataSimplex d=(StreamDataSimplex) data;
		multiplexer.addListener(d.channel, this);
		conn.launchStream(name);
	}
}
