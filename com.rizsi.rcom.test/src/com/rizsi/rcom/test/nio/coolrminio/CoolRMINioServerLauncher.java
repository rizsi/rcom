package com.rizsi.rcom.test.nio.coolrminio;

import java.net.InetSocketAddress;

import com.rizsi.rcom.test.nio.NioThread;
import com.rizsi.rcom.test.nio.example.Iremote;
import com.rizsi.rcom.test.nio.example.Remote;

import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

public class CoolRMINioServerLauncher {
	public static void main(String[] args) throws Exception {
		new CoolRMINioServerLauncher().run();
	}
	private void run() throws Exception
	{
		NioThread th=new NioThread();
		CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
		reg.addService(new CoolRMIService(Iremote.class.getName(), Iremote.class, new Remote()));
		new CoolRMINioServer().start(th, new InetSocketAddress("localhost", 9999), reg);
		th.start();
	}
}
