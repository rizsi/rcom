package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import com.rizsi.rcom.cli.AbstractCliArgs;

public class EchoCancellerAudioSystem extends AudioSystemAbstract
{
	MixingOutput mixing;
	Mic m;
	Play p;
	SpeexEchoCancel echo;
	public EchoCancellerAudioSystem(AbstractCliArgs args) throws LineUnavailableException, IOException {
		super(args);
		AudioFormat format=StreamSourceAudio.getFormat();
		
		final Mixer mixer = AudioSystem.getMixer(null);
		m=new Mic(mixer, format, StreamSourceAudio.requestBufferSize/2);
		p=new Play(mixer, format, StreamSourceAudio.requestBufferSize/2);
		echo=new SpeexEchoCancel();
		echo.setLog(true);
		echo.setup(m, p, StreamSourceAudio.requestBufferSize/2);
		echo.start();
		p.start();
		m.start();
		
		
		
		mixing=new MixingOutputSimple();
		mixing.start();
	}

	@Override
	public void startPlayback(ISyncAudioSource resampler) {
		mixing.addResampler(resampler);
	}

	@Override
	public ICapture startCapture(OutputStream os) {
		// TODO Auto-generated method stub
		return null;
	}
}
