package com.rizsi.rcom.test.nio.examplesimple;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.rizsi.rcom.VideoServerTCPListener;
import com.rizsi.rcom.test.nio.example.MainChannel;

import nio.NioThread;
import nio.multiplexer.ChannelProcessorMultiplexer;

public class ExampleNioClient {
	public static void main(String[] args) throws Exception {
		new ExampleNioClient().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		Thread.sleep(1000);
		SocketChannel sc=SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(new InetSocketAddress("localhost", 9999));
		ChannelProcessorMultiplexer m=new ChannelProcessorMultiplexer(nt, sc, true,
				VideoServerTCPListener.clientID.getBytes(StandardCharsets.UTF_8),
				VideoServerTCPListener.serverID.getBytes(StandardCharsets.UTF_8));
		m.start();
		MainChannel mc=new MainChannel();
		mc.register(m);
	}
}
