package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketProcess implements ISocket {
	private Process p;
	
	public SocketProcess(Process p) {
		super();
		this.p = p;
	}

	@Override
	public void close() throws IOException {
		p.destroy();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return p.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return p.getOutputStream();
	}

}
