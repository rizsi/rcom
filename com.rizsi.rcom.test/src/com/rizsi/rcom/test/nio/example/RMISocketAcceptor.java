package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import com.rizsi.rcom.VideoServerTCPListener;
import com.rizsi.rcom.test.nio.AbstractSocketAcceptor;
import com.rizsi.rcom.test.nio.ChannelProcessorMultiplexer;
import com.rizsi.rcom.test.nio.NioThread;

public class RMISocketAcceptor extends AbstractSocketAcceptor{
	RMINioConnectionServer niormi;
	public RMISocketAcceptor(NioThread t, ServerSocketChannel c, RMINioConnectionServer niormi) {
		super(t, c);
		this.niormi=niormi;
	}

	@Override
	protected void socketChannelAccepted(SocketChannel sc) throws IOException {
		sc.configureBlocking(false);
		ChannelProcessorMultiplexer m=new ChannelProcessorMultiplexer(t, sc, false, 
				VideoServerTCPListener.serverID.getBytes(StandardCharsets.UTF_8),
				VideoServerTCPListener.clientID.getBytes(StandardCharsets.UTF_8)
				);
		DuplexNioConnection conn=new DuplexNioConnection(m);
		niormi.connections.add(conn);
		try {
			m.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		MainChannel mc=new MainChannel();
//		mc.register(m);
//		try {
//			m.start();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Accepted!");
//		System.out.flush();
	}

	@Override
	public void close(Exception e) {
		System.err.println("TCP listen port closed");
		e.printStackTrace();
	}
}
