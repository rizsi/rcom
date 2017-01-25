package com.rizsi.rcom;

import java.io.Serializable;

import com.rizsi.rcom.cli.Client;

abstract public class StreamParameters implements Serializable
{
	private static final long serialVersionUID = 1L;
	public String name;
	public int sourceClient;
	
	public StreamParameters(String name, int sourceClient) {
		super();
		this.name = name;
		this.sourceClient=sourceClient;
	}
	abstract public StreamSink createSink(Client client);
	abstract public StreamShare createShare(VideoConnection videoConnection, int channel);
}
