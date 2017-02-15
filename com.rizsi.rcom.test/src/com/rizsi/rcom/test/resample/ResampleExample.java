package com.rizsi.rcom.test.resample;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.audio.SpeexResampler;
import com.rizsi.rcom.cli.UtilCli;
import com.rizsi.rcom.test.echocancel.ManualTestEchoCancel;
import com.rizsi.rcom.util.UtilStream;

import hu.qgears.commons.UtilFile;

public class ResampleExample {
	static int framesamples=256;
	public static void main(String[] args) throws Exception {
		AbstractRcomArgs a=new AbstractRcomArgs();
		UtilCli.parse(a, args, true);
		File folder=new File("/home/rizsi/tmp/video");
		byte[] data=UtilFile.loadFile(new File(folder, "remote.sw"));
		AudioFormat format=ManualTestEchoCancel.getFormat();
		final Mixer mixer = AudioSystem.getMixer(null);
		DataLine.Info info2= new DataLine.Info(SourceDataLine.class, format);
		SourceDataLine s=(SourceDataLine) mixer.getLine(info2);
		s.open(format, framesamples*2);
		s.start();
		try(LoopInputStream lis=new LoopInputStream(data))
		{
			try(SpeexResampler resampler=new SpeexResampler(a, framesamples, new ResampledReceiver(s)))
			{
				final byte[] buffer=new byte[framesamples*2];;
				while(true)
				{
					UtilStream.readFully(buffer, lis, buffer.length);
					feed(resampler, buffer);
				}
			}
//			byte[] buffer=new byte[framesamples*2];
//			while(true)
//			{
//				UtilStream.readFully(buffer, resampled, buffer.length);
//			}
		}
	}
	private static void feed(SpeexResampler resampler, byte[] buffer) throws Exception {
		resampler.feed(buffer, 8000,  9000);
	}
	static class ResampledReceiver implements SpeexResampler.ResampledReceiver
	{
		private SourceDataLine s;
		
		public ResampledReceiver(SourceDataLine s) {
			super();
			this.s = s;
		}

		@Override
		public void receiveResampled(byte[] data, int nsamples) {
//			System.out.println("Receive resampled: "+nsamples);
			s.write(data, 0, nsamples*2);
		}
		
	}
}
