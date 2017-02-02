package com.rizsi.rcom.test.nio.coolrminio;

import java.net.Socket;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;

import com.rizsi.rcom.test.nio.example.Iremote;

import nio.ConnectNio;
import nio.NioThread;
import nio.coolrmi.CoolRMINioClient;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.DualChannelProcessorMultiplexer;

public class CoolRMIHalfNioClientLauncher {
	public static void main(String[] args) throws Exception {
		new CoolRMIHalfNioClientLauncher().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		final Socket s=new Socket("localhost", 9999);
		SourceChannel in=ConnectNio.inputStreamToPipe(s.getInputStream());
		in.configureBlocking(false);
		SinkChannel out=ConnectNio.outputStreamToPipe(s.getOutputStream(), s);
		out.configureBlocking(false);
		DualChannelProcessorMultiplexer multiplexer=new DualChannelProcessorMultiplexer(nt, in, out, false, 
				CoolRMINioRemoter.clientId, CoolRMINioRemoter.serverId);
		CoolRMINioClient cli=new CoolRMINioClient(getClass().getClassLoader(), false);
		cli.connect(multiplexer);
		multiplexer.start();
		nt.start();
		Iremote r= (Iremote)cli.getService(Iremote.class, Iremote.class.getName());
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		cli.close();
		nt.close();
	}
}
