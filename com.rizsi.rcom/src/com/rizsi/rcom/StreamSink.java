package com.rizsi.rcom;

abstract public class StreamSink
{
	abstract public void dispose();

	abstract public void start(AbstractRcomArgs args, IVideocomConnection conn, ChannelMultiplexer multiplexer) throws Exception;
}
