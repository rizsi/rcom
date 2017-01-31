package com.rizsi.rcom.test.nio.example;

import java.io.IOException;

import hu.qgears.coolrmi.streams.IClientConnectionFactory;
import hu.qgears.coolrmi.streams.IConnection;
import nio.multiplexer.DuplexNioConnection;

public class RMINioClientConnectionFactory implements IClientConnectionFactory
{
	DuplexNioConnection conn;
	public RMINioClientConnectionFactory(DuplexNioConnection conn) {
		this.conn=conn;
	}

	@Override
	public IConnection connect() throws IOException {
		return conn;
	}

}
