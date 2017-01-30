package com.rizsi.rcom.test.nio.examplesimple;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.rizsi.rcom.VideoServerTCPListener;
import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;
import com.rizsi.rcom.test.nio.NioThread;

public class ExampleNioStdoutClient {
	public static void main(String[] args) throws Exception {
		new ExampleNioStdoutClient().run();
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
		new ConnectToStdout(m);
	}
}
