package com.rizsi.rcom.audio;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.StreamSinkSimplex;

public class StreamSinkAudio extends StreamSinkSimplex {
	private StreamParametersAudio p;
	private ISyncAudioSource resampler;
	private IAudioSystem audioSystem;
	private volatile IPlayback playback;

	public StreamSinkAudio(StreamParametersAudio p, AbstractRcomArgs abstractRcomArgs, IAudioSystem audioSystem) {
		super(p.name);
		this.p = p;
		this.audioSystem=audioSystem;
	}

	@Override
	public void start() throws Exception {
		playback=audioSystem.startPlayback(p, receiver);
	}

	@Override
	public void dispose() {
		if(playback!=null)
		{
			playback.close();
			playback=null;
		}
		if(resampler!=null)
		{
			resampler.close();
			resampler=null;
		}
		super.dispose();
	}

}
