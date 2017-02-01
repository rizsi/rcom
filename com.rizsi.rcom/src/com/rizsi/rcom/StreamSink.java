package com.rizsi.rcom;

import nio.multiplexer.IMultiplexer;

abstract public class StreamSink
{
	abstract public void dispose();

	abstract public void start(AbstractRcomArgs args, IVideocomConnection conn, IMultiplexer multiplexer) throws Exception;
}
