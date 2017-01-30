package com.rizsi.rcom.test.nio.coolrminio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.rizsi.rcom.test.nio.AbstractSocketAcceptor;
import com.rizsi.rcom.test.nio.NioThread;
import com.rizsi.rcom.test.nio.example.Iremote;
import com.rizsi.rcom.test.nio.example.Remote;

import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

public class CoolRMINioServer {
	public static void main(String[] args) throws Exception {
		new CoolRMINioServer().run();
	}
	private CoolRMIServiceRegistry reg;
	class SA extends AbstractSocketAcceptor
	{

		public SA(NioThread t, ServerSocketChannel c) {
			super(t, c);
		}

		@Override
		protected void socketChannelAccepted(SocketChannel sc) throws IOException {
			sc.configureBlocking(false);
			CoolRMINioRemoter srv=new CoolRMINioRemoter(getClass().getClassLoader(), false);
			srv.setServiceRegistry(reg);
			try {
				srv.connect(t, sc);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		@Override
		public void close(Exception e) {
			if(e!=null)
			{
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			
		}
		
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		Thread.sleep(1000);
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress("localhost", 9999));
		reg=new CoolRMIServiceRegistry();
		reg.addService(new CoolRMIService(Iremote.class.getName(), Iremote.class, new Remote()));
		SA sa=new SA(nt, ssc);
		sa.start();
	}
}
