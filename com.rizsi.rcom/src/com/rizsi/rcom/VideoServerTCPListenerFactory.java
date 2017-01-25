package com.rizsi.rcom;

import java.io.IOException;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.coolrmi.streams.IConnectionServer;
import hu.qgears.coolrmi.streams.IConnectionServerFactory;

public class VideoServerTCPListenerFactory implements IConnectionServerFactory {
	private ServerCliArgs args;
	
	public VideoServerTCPListenerFactory(ServerCliArgs args) {
		super();
		this.args = args;
	}

	@Override
	public IConnectionServer bindServer() throws IOException {
		return new VideoServerTCPListener(args);
	}

}
