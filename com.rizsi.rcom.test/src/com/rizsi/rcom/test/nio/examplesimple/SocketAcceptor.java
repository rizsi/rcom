package com.rizsi.rcom.test.nio.examplesimple;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import com.rizsi.rcom.VideoServerTCPListener;

import nio.AbstractSocketAcceptor;
import nio.NioThread;
import nio.multiplexer.ChannelProcessorMultiplexer;

public class SocketAcceptor extends AbstractSocketAcceptor{
	public SocketAcceptor(NioThread t, ServerSocketChannel c) {
		super(t, c);
	}

	@Override
	protected void socketChannelAccepted(SocketChannel sc) throws IOException {
		sc.configureBlocking(false);
		ChannelProcessorMultiplexer m=new ChannelProcessorMultiplexer(t, sc, false, 
				VideoServerTCPListener.serverID.getBytes(StandardCharsets.UTF_8),
				VideoServerTCPListener.clientID.getBytes(StandardCharsets.UTF_8)
				);
		MainChannel mc=new MainChannel();
		mc.register(m);
		try {
			m.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Accepted!");
		System.out.flush();
	}

	@Override
	public void close(Exception e) {
		e.printStackTrace();
	}


}
