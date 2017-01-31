package com.rizsi.rcom.test.nio.example;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import nio.NioThread;

public class RMINioServer {
	public static void main(String[] args) throws Exception {
		new RMINioServer().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		Thread.sleep(1000);
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress("localhost", 9999));
		RMINioConnectionServer niosrv=new RMINioConnectionServer();
		RMISocketAcceptor sa=new RMISocketAcceptor(nt, ssc, niosrv);
		sa.start();
		CoolRMIServer srv=new CoolRMIServer(getClass().getClassLoader(), new RMINioConnectionServerFactory(niosrv), false);
		srv.getServiceRegistry().addService(new CoolRMIService(Iremote.class.getName(), Iremote.class, new Remote()));
		srv.start();
	}
}
