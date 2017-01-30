package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import hu.qgears.coolrmi.streams.IConnection;
import hu.qgears.coolrmi.streams.IConnectionServer;

public class RMINioConnectionServer implements IConnectionServer
{
	public LinkedBlockingQueue<IConnection> connections=new LinkedBlockingQueue<>();
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConnection accept() throws IOException {
		try {
			return connections.take();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
