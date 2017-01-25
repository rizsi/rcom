package com.rizsi.rcom.vnc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import hu.qgears.commons.UtilProcess;

public class VncProxy {
	public static void main(String[] args) throws IOException {
		ServerSocket ss=new ServerSocket(5901);
		while(true)
		{
			Socket s=ss.accept();
			Socket o=new Socket("localhost", 5900);
			UtilProcess.streamErrorOfProcess(o.getInputStream(), s.getOutputStream());
			UtilProcess.streamErrorOfProcess(s.getInputStream(), new LogFilterOutpurStream(o.getOutputStream()));
		}
	}
}
