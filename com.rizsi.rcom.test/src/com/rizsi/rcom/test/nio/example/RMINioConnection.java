package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;

import hu.qgears.commons.UtilFile;
import hu.qgears.coolrmi.streams.IConnection;

public class RMINioConnection implements IConnection
{
	InputStreamReceiver is;
	OutputStreamSender os;
	public RMINioConnection(ChannelProcessorMultiplexer m) throws IOException {
		is=new InputStreamReceiver(UtilFile.defaultBufferSize.get()*8);
		os=new OutputStreamSender(m, UtilFile.defaultBufferSize.get()*8);
		is.register(m, 0);
	}
	@Override
	public InputStream getInputStream() throws IOException {
		return is.in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os.os;
	}

	@Override
	public void close() throws IOException {
		is.close(null);
		os.close(null);
	}
}
