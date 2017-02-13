package com.rizsi.rcom.test.resample;

import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.rizsi.rcom.test.echocancel.ManualTestEchoCancel;
import com.rizsi.rcom.util.UtilStream;

import hu.qgears.commons.UtilFile;

public class ResampleExample {
	static int framesamples=256;
	public static void main(String[] args) throws Exception {
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
			SpeexResampler resampler=new SpeexResampler();
			InputStream resampled= resampler.startResampling(framesamples, lis);
			byte[] buffer=new byte[framesamples*2];
			while(true)
			{
				UtilStream.readFully(buffer, resampled, buffer.length);
				s.write(buffer, 0, buffer.length);
			}
		}
	}
}
