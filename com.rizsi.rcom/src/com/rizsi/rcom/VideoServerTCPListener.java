package com.rizsi.rcom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.coolrmi.streams.IConnection;
import hu.qgears.coolrmi.streams.IConnectionServer;

public class VideoServerTCPListener implements IConnectionServer {
	public static final int port=9643;
	public static final String serviceID="RCOM0.0.0";
	public static final String clientID=serviceID+"client";
	public static final String serverID=serviceID+"server";
	private ServerSocket ss;
	
	public VideoServerTCPListener(ServerCliArgs args) throws IOException {
		super();
		ss=new ServerSocket();
		ss.bind(new InetSocketAddress(args.host, args.port));
	}
	@Override
	public void close() throws IOException {
		ss.close();
	}
	@Override
	public IConnection accept() throws IOException {
		Socket s=ss.accept();
		return new DemuxedConnection(new SocketSocket(s), true);
	}
}
