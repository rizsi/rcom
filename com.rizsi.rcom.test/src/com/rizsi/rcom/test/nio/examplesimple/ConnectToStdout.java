package com.rizsi.rcom.test.nio.examplesimple;

import java.io.IOException;

import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;
import com.rizsi.rcom.test.nio.example.InputStreamReceiver;
import com.rizsi.rcom.test.nio.example.OutputStreamSender;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;

public class ConnectToStdout {

	public ConnectToStdout(ChannelProcessorMultiplexer m) throws IOException {
		InputStreamReceiver isr=new InputStreamReceiver(UtilFile.defaultBufferSize.get()*8);
		isr.register(m, 0);
		ConnectStreams.startStreamThread(isr.in, System.out);
		OutputStreamSender oss=new OutputStreamSender(m, UtilFile.defaultBufferSize.get()*8);
		ConnectStreams.startStreamThread(System.in, oss.os);
	}

}
