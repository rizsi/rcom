package com.rizsi.rcom.test.nio.example;

import java.io.IOException;

import hu.qgears.coolrmi.streams.IConnectionServer;
import hu.qgears.coolrmi.streams.IConnectionServerFactory;

public class RMINioConnectionServerFactory implements IConnectionServerFactory{
	RMINioConnectionServer srv;
	
	public RMINioConnectionServerFactory(RMINioConnectionServer srv) {
		super();
		this.srv = srv;
	}

	@Override
	public IConnectionServer bindServer() throws IOException {
		return srv;
	}

}
