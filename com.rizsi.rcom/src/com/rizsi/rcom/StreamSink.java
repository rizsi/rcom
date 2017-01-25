package com.rizsi.rcom;

abstract public class StreamSink
{
	abstract public void dispose();

	abstract public void start(IVideocomConnection conn, ChannelMultiplexer multiplexer) throws Exception;
}
