package com.rizsi.rcom;

import java.io.Serializable;

public class StreamDataSimplex implements IStreamData, Serializable {
	private static final long serialVersionUID = 1L;
	public final int channel;

	public StreamDataSimplex(int channel) {
		super();
		this.channel = channel;
	}
	
}
