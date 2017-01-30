package com.rizsi.rcom.test.nio.coolrminio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.rizsi.rcom.test.nio.AbstractSocketAcceptor;
import com.rizsi.rcom.test.nio.NioThread;
import com.rizsi.rcom.test.nio.example.Iremote;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

public class CoolRMINioClient {
	public static void main(String[] args) throws Exception {
		new CoolRMINioClient().run();
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
		SocketChannel sc=SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(new InetSocketAddress("localhost", 9999));
		CoolRMINioRemoter srv=new CoolRMINioRemoter(getClass().getClassLoader(), false);
		srv.connect(nt, sc);
//		m.start();
//		CoolRMIClient client=new CoolRMIClient(getClass().getClassLoader(), new RMINioClientConnectionFactory(conn), false);
		Iremote r= (Iremote)srv.getService(Iremote.class, Iremote.class.getName());
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));

		
		
		
//		NioThread nt=new NioThread();
//		nt.start();
//		Thread.sleep(1000);
//		ServerSocketChannel ssc=ServerSocketChannel.open();
//		ssc.configureBlocking(false);
//		ssc.bind(new InetSocketAddress("localhost", 9999));
//		reg=new CoolRMIServiceRegistry();
//		reg.addService(new CoolRMIService(Iremote.class.getName(), Iremote.class, new Remote()));
//		SA sa=new SA(nt, ssc);
//		sa.start();
	}
}
