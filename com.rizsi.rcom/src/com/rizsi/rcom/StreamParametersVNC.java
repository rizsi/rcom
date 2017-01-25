package com.rizsi.rcom;

import com.rizsi.rcom.cli.Client;

public class StreamParametersVNC extends StreamParameters {
	private static final long serialVersionUID = 1L;

	public StreamParametersVNC(String name, int sourceClient) {
		super(name, sourceClient);
	}
	@Override
	public String toString() {
		return ""+name;
	}
	@Override
	public StreamSink createSink(Client c) {
		return new StreamSinkVNC(this);
	}
	@Override
	public StreamShare createShare(VideoConnection videoConnection, int channel) {
		return new StreamShareVNC(videoConnection, channel, this);
	}

}
