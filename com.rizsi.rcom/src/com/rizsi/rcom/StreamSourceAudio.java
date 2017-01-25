package com.rizsi.rcom;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;
import com.rizsi.rcom.cli.Client;

public class StreamSourceAudio implements AutoCloseable {
	public static AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 8;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	public static final int bufferSize=1000;
	private IVideocomConnection conn;
	private StreamParametersAudio params;
	private AudioFormat format = getFormat();
	private volatile boolean running = true;

	public void start(Client client, String streamName) {
		conn=client.conn;
		ChannelOutputStream cos = client.fact.getMultiplexer().createStream();
		client.conn.shareStream(cos.getChannel(), params=new StreamParametersAudio(streamName, client.id));
		new Thread("Audio capture") {
			public void run() {
				runCapture(cos);
			};
		}.start();
	}

	protected void runCapture(ChannelOutputStream cos) {
		try {
			byte buffer[] = new byte[bufferSize];
			System.out.println("Buffer size: "+bufferSize+" "+format.getSampleRate());
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			try(final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info))
			{
				line.open(format);
				line.start();
				while (running) {
					int count = line.read(buffer, 0, buffer.length);
					if (count > 0) {
						cos.write(buffer, 0, count);
						cos.flush();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally
		{
			try {
				cos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		if(params!=null)
		{
			conn.unshare(params);
			params=null;
		}
		running=false;
	}

}
