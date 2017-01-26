package com.rizsi.rcom;

import java.io.IOException;
import java.net.Socket;

import com.rizsi.rcom.cli.AbstractCliArgs;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.coolrmi.streams.IClientConnectionFactory;
import hu.qgears.coolrmi.streams.IConnection;

public class VideoClientConnectionFactory implements IClientConnectionFactory
{
	private DemuxedConnection conn;
	private AbstractCliArgs args;
	
	public VideoClientConnectionFactory(AbstractCliArgs args) {
		super();
		this.args = args;
	}
	@Override
	public IConnection connect() throws IOException {
		ISocket s;
		if(args.ssh!=null)
		{
			Process p=Runtime.getRuntime().exec(args.program_ssh+" "+args.ssh);
			s=new SocketProcess(p);
			ConnectStreams.startStreamThread(p.getErrorStream(), System.err);
		}else
		{
			s=new SocketSocket(new Socket(args.host, args.port));
		}
		return conn=new DemuxedConnection(s, false);
	}
	public ChannelMultiplexer getMultiplexer()
	{
		return conn.getMultiplexer();
	}

}
