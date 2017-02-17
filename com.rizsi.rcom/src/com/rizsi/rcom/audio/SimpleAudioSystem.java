package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.rizsi.rcom.cli.AbstractCliArgs;
import com.rizsi.rcom.util.UtilStream;

import nio.multiplexer.InputStreamReceiver;

public class SimpleAudioSystem implements IAudioSystem
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
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
				try(final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info))
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
	
	class Playback implements IPlayback
	{
		private ISyncAudioSource resampler;
		private InputStreamReceiver receiver;
		public Playback(StreamParametersAudio p, InputStreamReceiver receiver) {
			this.receiver=receiver;
		}
		void start() throws Exception
		{
			if(!args.disableAudioJitterResampler)
			{
				final JitterResampler jr=new JitterResampler(args, (int)StreamSourceAudio.getFormat().getSampleRate(), StreamSourceAudio.requestBufferSize/2, 2);
				resampler=jr;
				new Thread("Audio jitter resampler")
				{
					private byte[] buffer;
					@Override
					public void run() {
						try {
							buffer=new byte[StreamSourceAudio.requestBufferSize];
							while(!jr.isClosed())
							{
								UtilStream.readFully(buffer, receiver.in, buffer.length);
								jr.writeInput(buffer);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
			}else
			{
				resampler=new ZeroResampler(receiver);
			}
			startPlayback(resampler);
		}
		@Override
		public void close() {
			resampler.close();
		}
	}
	@Override
	public void startPlayback(final ISyncAudioSource resampler) {
		final AudioFormat format = StreamSourceAudio.getFormat();
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
	private AbstractCliArgs args;
	

	public SimpleAudioSystem(AbstractCliArgs args) {
		super();
		this.args = args;
	}

	@Override
	public ICapture startCapture(OutputStream os) {
		Capture c=new Capture(os);
		c.start();
		return c;
	}

	@Override
	public IPlayback startPlayback(StreamParametersAudio p, InputStreamReceiver receiver) throws Exception {
		Playback ret=new Playback(p, receiver);
		ret.start();
		return ret;
	}

}
