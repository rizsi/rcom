package com.rizsi.rcom.test.nio.coolrminio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.rizsi.rcom.test.nio.AbstractSocketAcceptor;
import com.rizsi.rcom.test.nio.NioThread;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

public class CoolRMINioServer {
	private CoolRMIServiceRegistry reg;
	private SA sa;
	public class SA extends AbstractSocketAcceptor
	{

		public SA(NioThread t, ServerSocketChannel c) {
			super(t, c);
		}

		@Override
		protected void socketChannelAccepted(SocketChannel sc) throws IOException {
			sc.configureBlocking(false);
			CoolRMINioRemoter srv=new CoolRMINioRemoter(getClass().getClassLoader(), false, true);
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
	public void start(NioThread nt, SocketAddress address, CoolRMIServiceRegistry reg) throws Exception
	{
		this.reg=reg;
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(address);
		sa=new SA(nt, ssc);
		sa.start();
	}
}
