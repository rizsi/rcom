package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;

import hu.qgears.commons.UtilProcess;

public class StreamSinkVNC extends StreamSink implements IChannelReader
{

	StreamParametersVNC streamParametersVNC;
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	public StreamSinkVNC(StreamParametersVNC streamParametersVNC) {
		this.streamParametersVNC=streamParametersVNC;
	}

	@Override
	public void dispose() {
		if(p!=null)
		{
			p.destroy();
			p=null;
		}
	}
	private InputStream is;
	private OutputStream os;
	private Process p;
	@Override
	public void start(IVideocomConnection conn, ChannelMultiplexer multiplexer) throws Exception {
		int n=9;
		int port=5900+n;
		Socket s;
		try(ServerSocket ss=new ServerSocket())
		{
			ss.bind(new InetSocketAddress("localhost", port));
			p=Runtime.getRuntime().exec("xvnc4viewer -ViewOnly localhost:"+n);
			UtilProcess.streamErrorOfProcess(p.getInputStream(), System.out);
			UtilProcess.streamErrorOfProcess(p.getErrorStream(), System.err);
			s=ss.accept();
		}
		is=s.getInputStream();
		os=s.getOutputStream();
		ChannelOutputStream cos=multiplexer.createStream();
		StreamDataDuplex stream=(StreamDataDuplex)conn.registerStream(streamParametersVNC.name, cos.getChannel());
		multiplexer.addListener(stream.backChannel, this);
		conn.launchStream(streamParametersVNC.name);
		UtilProcess.streamErrorOfProcess(is, cos);
	}

	@Override
	public void readFully(InputStream is, int len) throws IOException {
		IChannelReader.pipeToFully(is, len, buffer, os);
	}

}