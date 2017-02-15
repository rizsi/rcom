package com.rizsi.rcom.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.StreamSinkSimplex;
import com.rizsi.rcom.util.UtilStream;

public class StreamSinkAudio extends StreamSinkSimplex {
	private StreamParametersAudio p;
	private AudioFormat format;
	private volatile boolean exit;
	private JitterResampler resampler;
	private AbstractRcomArgs args;

	public StreamSinkAudio(StreamParametersAudio p, AbstractRcomArgs abstractRcomArgs) {
		super(p.name);
		this.p = p;
		args=abstractRcomArgs;
	}

	@Override
	public void start() throws Exception {
		format = StreamSourceAudio.getFormat();
		resampler=new JitterResampler(args, (int)format.getSampleRate(), StreamSourceAudio.requestBufferSize/2, 2);
		new Thread("Audio jitter resampler")
		{
			private byte[] buffer;
			@Override
			public void run() {
				try {
					buffer=new byte[StreamSourceAudio.requestBufferSize];
					while(!exit)
					{
						UtilStream.readFully(buffer, receiver.in, buffer.length);
						resampler.writeInput(buffer);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		new Thread("Audio output") {
			private byte[] buffer;
			public void run() {
				try {
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
							resampler.readOutput(buffer);
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
		if(resampler!=null)
		{
			resampler.close();
			resampler=null;
		}
		super.dispose();
	}

}
