package com.rizsi.rcom.audio;

import java.io.OutputStream;

import nio.multiplexer.InputStreamReceiver;

public interface IAudioSystem {

	void startPlayback(ISyncAudioSource resampler);

	ICapture startCapture(OutputStream os);

	IPlayback startPlayback(StreamParametersAudio p, InputStreamReceiver receiver) throws Exception;

}
