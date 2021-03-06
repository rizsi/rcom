package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.rizsi.rcom.cli.AbstractCliArgs;

public class SimpleAudioSystem extends AudioSystemAbstract
{
	class Capture extends Thread implements ICapture
	{
		private final OutputStream oss;
		private volatile boolean closed;
		public Capture(OutputStream oss)
		{
			super("Audio capture");
			this.oss=oss;
		}
		@Override
		public void run() {
			try {
				AudioFormat format=StreamSourceAudio.getFormat();
				try(final TargetDataLine line = args.platform.openTargetDataLine())
				{
					line.open(format, StreamSourceAudio.requestBufferSize);
					byte buffer[] = new byte[line.getBufferSize()];
					System.out.println("Buffer size: "+buffer.length+" "+format.getSampleRate());
					line.start();
					while (!closed) {
						int count = line.read(buffer, 0, buffer.length);
						if (count > 0) {
							oss.write(buffer, 0, count);
							oss.flush();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally
			{
				try {
					oss.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		@Override
		public void close() {
			closed=true;
		}
	}
	
	@Override
	public void startPlayback(final ISyncAudioSource resampler) {
		final AudioFormat format = StreamSourceAudio.getFormat();
		new Thread("Audio output") {
			private byte[] buffer;
			public void run() {
				try {
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
					try(SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info))
					{
						line.open(format, StreamSourceAudio.requestBufferSize);
						line.start();
						buffer=new byte[line.getBufferSize()];
						while(!resampler.isClosed())
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
	public SimpleAudioSystem(AbstractCliArgs args) {
		super(args);
	}

	@Override
	public ICapture startCapture(OutputStream os) {
		Capture c=new Capture(os);
		c.start();
		return c;
	}
}
