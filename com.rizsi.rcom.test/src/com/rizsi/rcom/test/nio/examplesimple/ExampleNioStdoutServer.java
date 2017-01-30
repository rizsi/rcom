package com.rizsi.rcom.test.nio.examplesimple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import com.rizsi.rcom.VideoServerTCPListener;
import com.rizsi.rcom.test.nio.AbstractSocketAcceptor;
import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;
import com.rizsi.rcom.test.nio.NioThread;

public class ExampleNioStdoutServer {
	public static void main(String[] args) throws Exception {
		new ExampleNioStdoutServer().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		Thread.sleep(1000);
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress("localhost", 9999));
		AbstractSocketAcceptor sa=new AbstractSocketAcceptor(nt, ssc) {
			
			@Override
			protected void socketChannelAccepted(SocketChannel sc) throws IOException {
				sc.configureBlocking(false);
				ChannelProcessorMultiplexer m=new ChannelProcessorMultiplexer(t, sc, false, 
						VideoServerTCPListener.serverID.getBytes(StandardCharsets.UTF_8),
						VideoServerTCPListener.clientID.getBytes(StandardCharsets.UTF_8)
						);

				new ConnectToStdout(m);
				try {
					m.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void close(Exception e) {
				e.printStackTrace();
			}
		};
		sa.start();
	}

}
