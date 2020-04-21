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
		if(!c.getArgs().disableVNC)
		{
			return new StreamSinkVNC(this);
		}else
		{
			return new StreamSinkDummy();
		}
	}
	@Override
	public StreamShare createShare(VideoConnection videoConnection, int channel) {
		if(!videoConnection.getArgs().disableVNC)
		{
			return new StreamShareVNC(videoConnection, channel, this);
		}else
		{
			return new StreamShareDummy(videoConnection, this);
		}
	}

}
