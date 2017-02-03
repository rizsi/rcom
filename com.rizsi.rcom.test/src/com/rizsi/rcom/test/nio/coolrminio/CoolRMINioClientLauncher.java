package com.rizsi.rcom.test.nio.coolrminio;

import java.net.InetSocketAddress;

import com.rizsi.rcom.test.nio.example.Iremote;

import nio.NioThread;
import nio.coolrmi.CoolRMINioClient;
import nio.coolrmi.CoolRMINioRemoter;

public class CoolRMINioClientLauncher {
	public static void main(String[] args) throws Exception {
		new CoolRMINioClientLauncher().run();
	}
	private void run() throws Exception
	{
		NioThread nt=new NioThread();
		nt.start();
		CoolRMINioClient cli=new CoolRMINioClient(getClass().getClassLoader(), false);
		cli.connect(nt, new InetSocketAddress("localhost", 9999), CoolRMINioRemoter.clientId, CoolRMINioRemoter.serverId);
		Iremote r= (Iremote)cli.getService(Iremote.class, Iremote.class.getName());
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		System.out.println(""+r.getValue("Kitten"));
		cli.close();
		nt.close();
	}
}
