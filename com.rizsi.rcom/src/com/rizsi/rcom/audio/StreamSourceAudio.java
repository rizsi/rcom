package com.rizsi.rcom.audio;

import javax.sound.sampled.AudioFormat;

import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.cli.Client;

import nio.multiplexer.OutputStreamSender;

public class StreamSourceAudio implements AutoCloseable {
	public static AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	private IVideocomConnection conn;
	private StreamParametersAudio params;
	public static final int requestBufferSize=512;
	private volatile ICapture capture;
	private OutputStreamSender oss;

	public void start(Client client, String streamName) {
		conn=client.conn;
		oss=new OutputStreamSender(client.getMultiplexer(), VideoConnection.bufferSize, false);
		client.conn.shareStream(oss.getId(), params=new StreamParametersAudio(streamName, client.id));
		capture=client.getAudio().startCapture(oss.os);
	}

	@Override
	public void close() {
		if(capture!=null)
		{
			capture.close();
			capture=null;
		}
		if(params!=null)
		{
			conn.unshare(params);
			params=null;
		}
		oss.close(null);
	}

}
