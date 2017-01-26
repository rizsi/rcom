package com.rizsi.rcom;

import com.rizsi.rcom.cli.Client;

public class StreamParametersVideo extends StreamParameters {
	private static final long serialVersionUID = 1L;
	public int width;
	public int height;
	public int framerate=20;
	public String encoding;

	public StreamParametersVideo(String name, int sourceClient, int width, int height, String encoding) {
		super(name, sourceClient);
		this.width = width;
		this.height = height;
		this.encoding=encoding;
	}
	@Override
	public String toString() {
		return ""+name+" "+width+"x"+height;
	}
	@Override
	public StreamSink createSink(Client c) {
		if(c.isGUI())
		{
			return new StreamSinkVideoFrames(c.getArgs(), this);
		}else
		{
			return new StreamSinkVideo(this);
		}
	}
	@Override
	public StreamShare createShare(VideoConnection videoConnection, int channel) {
		return new StreamShareSimplex(videoConnection, channel, this);
	}
}
