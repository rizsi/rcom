package com.rizsi.rcom;

import java.io.Serializable;

public class StreamDataDuplex implements IStreamData, Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Channel from client to server.
	 */
	public final int channel;
	/**
	 * Channel from server to client.
	 */
	public final int backChannel;

	public StreamDataDuplex(int channel, int backChannel) {
		super();
		this.channel = channel;
		this.backChannel=backChannel;
	}
	
}
