package com.rizsi.rcom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.rizsi.rcom.util.UtilStream;

public class StreamSinkAudio extends StreamSinkSimplex {
	StreamParameters p;
	private byte[] buffer;
	private AudioFormat format;
	private volatile boolean exit;

	public StreamSinkAudio(StreamParametersAudio p) {
		super(p.name);
		this.p = p;
	}

	@Override
	public void start() throws Exception {
		new Thread("Audio output") {
			public void run() {
				try {
					format = StreamSourceAudio.getFormat();
					// final AudioInputStream ais =
					// new AudioInputStream(input, format,
					// audio.length / format.getFrameSize());
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
					try(SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info))
					{
						line.open(format, StreamSourceAudio.requestBufferSize);
						line.start();
						buffer=new byte[line.getBufferSize()];
						while(!exit)
						{
							UtilStream.readFully(buffer, receiver.in, buffer.length);
							line.write(buffer, 0, buffer.length);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}

	@Override
	public void dispose() {
		exit=true;
		super.dispose();
	}

}
