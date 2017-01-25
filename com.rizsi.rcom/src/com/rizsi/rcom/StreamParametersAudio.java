package com.rizsi.rcom;

import com.rizsi.rcom.cli.Client;

public class StreamParametersAudio extends StreamParameters {
	private static final long serialVersionUID = 1L;

	public StreamParametersAudio(String name, int sourceClient) {
		super(name, sourceClient);
	}
	@Override
	public String toString() {
		return ""+name+" audio";
	}
	@Override
	public StreamSink createSink(Client c) {
		return new StreamSinkAudio(this);
	}
	@Override
	public StreamShare createShare(VideoConnection videoConnection, int channel) {
		return new StreamShareSimplex(videoConnection, channel, this);
	}
}
