package com.rizsi.rcom.audio;

import com.rizsi.rcom.cli.AbstractCliArgs;
import com.rizsi.rcom.util.UtilStream;

import nio.multiplexer.InputStreamReceiver;

abstract public class AudioSystemAbstract  implements IAudioSystem
{
	final protected AbstractCliArgs args;

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
	

	public AudioSystemAbstract(AbstractCliArgs args) {
		super();
		this.args = args;
	}

	@Override
	public IPlayback startPlayback(StreamParametersAudio p, InputStreamReceiver receiver) throws Exception {
		Playback ret=new Playback(p, receiver);
		ret.start();
		return ret;
	}
}
