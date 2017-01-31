package com.rizsi.rcom.test.nio.examplesimple;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import nio.NioThread;

public class ExampleNioServer {
	public static void main(String[] args) throws Exception {
		new ExampleNioServer().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		Thread.sleep(1000);
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress("localhost", 9999));
		SocketAcceptor sa=new SocketAcceptor(nt, ssc);
		sa.start();
	}
}
