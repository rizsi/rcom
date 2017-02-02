package com.rizsi.rcom.test.nio.examplesimple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import hu.qgears.commons.ConnectStreams;

/**
 * Mini manual test app for trying TCP prefixer script.
 * @author rizsi
 *
 */
public class SimpleServer {
	public static void main(String[] args) throws Exception {
		ServerSocket ss=new ServerSocket();
		ss.bind(new InetSocketAddress("localhost", 9999));
		Socket s=ss.accept();
		InputStream is=s.getInputStream();
		OutputStream os=s.getOutputStream();
		ConnectStreams.startStreamThread(is, System.out);
		ConnectStreams.startStreamThread(System.in, os);
	}
}
