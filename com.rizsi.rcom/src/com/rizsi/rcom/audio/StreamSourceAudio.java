package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.cli.Client;

import nio.multiplexer.OutputStreamSender;

public class StreamSourceAudio implements AutoCloseable {
	public static AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 8;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	private IVideocomConnection conn;
	private StreamParametersAudio params;
	private AudioFormat format = getFormat();
	public static final int requestBufferSize=512;
	private volatile boolean running = true;
	private OutputStreamSender oss;

	public void start(Client client, String streamName) {
		conn=client.conn;
		oss=new OutputStreamSender(client.getMultiplexer(), VideoConnection.bufferSize, false);
		client.conn.shareStream(oss.getId(), params=new StreamParametersAudio(streamName, client.id));
		new Thread("Audio capture") {
			public void run() {
				runCapture(oss.os);
			};
		}.start();
	}

	protected void runCapture(OutputStream cos) {
		try {
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			try(final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info))
			{
				line.open(format, requestBufferSize);
				byte buffer[] = new byte[line.getBufferSize()];
				System.out.println("Buffer size: "+buffer.length+" "+format.getSampleRate());
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
		oss.close(null);
	}

}
