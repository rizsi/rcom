package com.rizsi.rcom.test.nio.coolrminio;

import java.net.InetSocketAddress;

import com.rizsi.rcom.test.nio.example.Iremote;
import com.rizsi.rcom.test.nio.example.Remote;

import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import nio.NioThread;
import nio.coolrmi.CoolRMINioServer;

public class CoolRMINioServerLauncher {
	public static void main(String[] args) throws Exception {
		new CoolRMINioServerLauncher().run();
	}
	private void run() throws Exception
	{
		NioThread th=new NioThread();
		CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
		reg.addService(new CoolRMIService(Iremote.class.getName(), Iremote.class, new Remote()));
		new CoolRMINioServer(getClass().getClassLoader(), reg).listen(th, new InetSocketAddress("localhost", 9999));
		th.start();
	}
}
