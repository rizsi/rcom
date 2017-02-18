package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import com.rizsi.rcom.NullOutputStream;
import com.rizsi.rcom.cli.AbstractCliArgs;

public class EchoCancellerAudioSystem extends AudioSystemAbstract
{
	MixingOutput mixing;
	Mic m;
	SpeexEchoCancel echo;
	public class Capture implements ICapture
	{

		@Override
		public void close() {
			// TODO in case we are already recording an other then it may cause problem.
			echo.setProcessed(new NullOutputStream());
		}
		
	}
	public EchoCancellerAudioSystem(AbstractCliArgs args) throws LineUnavailableException, IOException {
		super(args);
		AudioFormat format=StreamSourceAudio.getFormat();
		
		final Mixer mixer = AudioSystem.getMixer(null);
		m=new Mic(args, mixer, format, StreamSourceAudio.requestBufferSize/2);
		mixing=new MixingOutputSimple(mixer, args);
		echo=new SpeexEchoCancel();
		//echo.setLog(true);
		echo.setup(args, m, mixing, StreamSourceAudio.requestBufferSize/2);
		echo.start();
		mixing.start();
		m.start();
	}

	@Override
	public void startPlayback(ISyncAudioSource resampler) {
		mixing.addResampler(resampler);
	}

	@Override
	public ICapture startCapture(OutputStream os) {
//		m.setRecord(os);
		echo.setProcessed(os);
		return new Capture();
	}
}
