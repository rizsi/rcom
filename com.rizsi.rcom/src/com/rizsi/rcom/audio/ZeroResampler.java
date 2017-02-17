package com.rizsi.rcom.audio;

import java.io.IOException;

import nio.multiplexer.InputStreamReceiver;

public class ZeroResampler implements ISyncAudioSource
{
	private volatile boolean closed;
	private InputStreamReceiver receiver;
	public ZeroResampler(InputStreamReceiver receiver) {
		this.receiver=receiver;
	}

	@Override
	public void readOutput(byte[] data) throws IOException {
		int avail=receiver.in.available();
		int n=Math.min(avail, data.length);
		if(n>0)
		{
			receiver.in.read(data, 0, n);
		}
		for(int i=n;i<data.length;++i)
		{
			data[i]=0;
		}
	}

	@Override
	public void close() {
		closed=true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

}
