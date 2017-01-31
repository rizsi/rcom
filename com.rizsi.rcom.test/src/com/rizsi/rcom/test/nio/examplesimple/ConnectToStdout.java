package com.rizsi.rcom.test.nio.examplesimple;

import java.io.IOException;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;
import nio.multiplexer.ChannelProcessorMultiplexer;
import nio.multiplexer.InputStreamReceiver;
import nio.multiplexer.OutputStreamSender;

public class ConnectToStdout {

	public ConnectToStdout(ChannelProcessorMultiplexer m) throws IOException {
		InputStreamReceiver isr=new InputStreamReceiver(UtilFile.defaultBufferSize.get()*8);
		isr.register(m, 0);
		ConnectStreams.startStreamThread(isr.in, System.out);
		OutputStreamSender oss=new OutputStreamSender(m, UtilFile.defaultBufferSize.get()*8);
		ConnectStreams.startStreamThread(System.in, oss.os);
	}

}
