package com.rizsi.rcom.test.nio.example;

import java.io.IOException;

import hu.qgears.coolrmi.streams.IClientConnectionFactory;
import hu.qgears.coolrmi.streams.IConnection;

public class RMINioClientConnectionFactory implements IClientConnectionFactory
{
	RMINioConnection conn;
	public RMINioClientConnectionFactory(RMINioConnection conn) {
		this.conn=conn;
	}

	@Override
	public IConnection connect() throws IOException {
		return conn;
	}

}
