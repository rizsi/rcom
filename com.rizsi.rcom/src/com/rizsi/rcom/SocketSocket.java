package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketSocket implements ISocket{
	private Socket s;
	

	public SocketSocket(Socket s) {
		super();
		this.s = s;
	}

	@Override
	public void close() throws IOException {
		s.close();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return s.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return s.getOutputStream();
	}

}
