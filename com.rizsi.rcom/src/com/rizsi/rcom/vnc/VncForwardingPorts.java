package com.rizsi.rcom.vnc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class VncForwardingPorts {
	/**
	 * X11vnc reflect instance connects to this VNC server(port=5900+n) to reflect the VNC server that is there.
	 */
	private int n;
	/**
	 * X11vnc reflect instance opens this server port where clients can connect to.
	 */
	private int rfbport;
	public VncForwardingPorts() {
	}
	/**
	 * TCP port where remote VNC server is forwarded to.
	 * @return
	 */
	public int getLocalport() {
		return 5900+n;
	}
	public void dispose() {
		// Nothing to do: port allocation is done in OS
	}
	public int getN() {
		return n;
	}
	public void bindLocalPort(ServerSocket ss) throws IOException {
		for(int i=0;i<2000;++i)
		{
			try {
				ss.bind(new InetSocketAddress("localhost", 5910+i));
				break;	// Server Socket is bound to a port successfully
			} catch (IOException e) {
				// Silent go on to next possible port
			}
		}
		int lp=ss.getLocalPort();
		n=lp-5900;
		System.out.println("VNC local portforward port: "+lp+" N: "+n);
	}
	/**
	 * Allocate a TCP port that is possible to host the X11vnc recast server
	 * @return
	 * @throws IOException
	 */
	public int allocateRfbport() throws IOException {
		try(ServerSocket ss=new ServerSocket())
		{
			ss.bind(new InetSocketAddress("localhost", 0));
			rfbport=ss.getLocalPort();
			return rfbport;
		}
	}
	public int getRfbport() {
		return rfbport;
	}
}
