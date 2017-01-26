package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;
import com.rizsi.rcom.cli.Client;
import com.rizsi.rcom.vnc.LogFilterOutpurStream;

import hu.qgears.commons.UtilProcess;

public class StreamSourceVnc implements IChannelReader, AutoCloseable {
	private Socket s;
	OutputStream os;
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	ChannelOutputStream cos;
	StreamParameters params;
	IVideocomConnection conn;
	public void start(Client client, String streamName) throws IOException {
		conn=client.conn;
		cos = client.fact.getMultiplexer().createStream();
		ServerSocket ss=new ServerSocket();
		ss.bind(new InetSocketAddress("localhost", 0));
		int localport=ss.getLocalPort();
		System.out.println("Local port: "+localport);
		params=new StreamParametersVNC(streamName, client.id);
		StreamDataDuplex data=(StreamDataDuplex)client.conn.shareStream(cos.getChannel(), params);
		client.fact.getMultiplexer().addListener(data.backChannel, this);
		Process p=Runtime.getRuntime().exec(client.getArgs().program_x11vnc+" -connect localhost:"+localport
				// +" -clip 200x200+50+50"
				+" -localhost");
		UtilProcess.streamErrorOfProcess(p.getErrorStream(), System.err);
		UtilProcess.streamErrorOfProcess(p.getInputStream(), System.out);
		s=ss.accept();
		os=new LogFilterOutpurStream(s.getOutputStream());
		System.out.println("CONNECTED!");
		UtilProcess.streamErrorOfProcess(s.getInputStream(), cos);
		ss.close();
	}

	@Override
	public void readFully(InputStream is, int len) throws IOException {
		IChannelReader.pipeToFully(is, len, buffer, os);
	}
	@Override
	public void close() {
		if(params!=null)
		{
			conn.unshare(params);
			params=null;
		}
		if(s!=null)
		{
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			s=null;
		}
		// VNC process closes itself when the socket is closed.
		if(cos!=null)
		{
			try {
				cos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cos=null;
		}
	}
}
