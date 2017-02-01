package com.rizsi.rcom;

import nio.multiplexer.ChannelProcessorMultiplexer;

abstract public class StreamSink
{
	abstract public void dispose();

	abstract public void start(AbstractRcomArgs args, IVideocomConnection conn, ChannelProcessorMultiplexer multiplexer) throws Exception;
}
