package com.rizsi.rcom.test.nio.coolrminio;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import com.rizsi.rcom.test.nio.NioThread;

public class CoolRMINioClient extends CoolRMINioRemoter
{
	public CoolRMINioClient(ClassLoader classLoader, boolean guaranteeOrdering) {
		super(classLoader, guaranteeOrdering);
	}

	public void connect(NioThread nt, SocketAddress address) throws Exception
	{
		SocketChannel sc=SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(address);
		connect(nt, sc);
	}
}
