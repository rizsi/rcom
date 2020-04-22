package com.rizsi.rcom;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.rizsi.rcom.cli.Client;
import com.rizsi.rcom.util.ChainList;
import com.rizsi.rcom.vnc.LogFilterOutpurStream;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilString;
import nio.multiplexer.InputStreamReceiver;
import nio.multiplexer.OutputStreamSender;

public class StreamSourceVnc implements AutoCloseable {
	private Socket s;
	private OutputStream os;
	private OutputStreamSender oss;
	private InputStreamReceiver isr;
	private StreamParameters params;
	private IVideocomConnection conn;
	public void start(Client client, String streamName) throws IOException {
		conn=client.conn;
		oss=new OutputStreamSender(client.getMultiplexer(), StreamShareVNC.bufferSize, true);
		try(ServerSocket ss=new ServerSocket())
		{
			ss.bind(new InetSocketAddress("localhost", 0));
			int localport=ss.getLocalPort();
			System.out.println("Local port: "+localport);
			params=new StreamParametersVNC(streamName, client.id);
			int id=oss.getId();
			IStreamData strd=client.conn.shareStream(id, params);
			if(!(strd instanceof StreamDataDuplex))
			{
				throw new RuntimeException("Server does not support VNC!");
			}
			StreamDataDuplex data=(StreamDataDuplex)strd;
			isr=new InputStreamReceiver(StreamShareVNC.bufferSize, true);
			client.getMultiplexer().register(isr, data.backChannel);
			ChainList<String> command=new ChainList<>(client.getArgs().program_x11vnc, "-connect_or_exit", "localhost:"+localport, "-rfbport", "0");
			//command.addcs("-clip", "200x200+50+50");
			// command.add("-localhost");
			command.addcs("-rfbportv6", "-1"); // See: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=672449
			command.add("-noremote"); // Remote control is disabled
			System.out.println("Command: "+UtilString.concat(command, " "));
			new ProcessBuilder(command).redirectError(Redirect.INHERIT)
					.redirectOutput(Redirect.INHERIT)
					.start();
			s=ss.accept();
			os=new LogFilterOutpurStream(s.getOutputStream());
			System.out.println("CONNECTED!");
			ConnectStreams.startStreamThread(s.getInputStream(), oss.os);
			ConnectStreams.startStreamThread(isr.in, os);
		}
	}
	@Override
	public void close() {
		if(params!=null)
		{
			try {
				conn.unshare(params);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		if(isr!=null)
		{
			try {
				isr.close(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isr=null;
		}
	}
}
