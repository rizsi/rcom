package com.rizsi.rcom.test.echocancel;

import java.io.File;
import java.nio.ByteOrder;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import hu.qgears.commons.UtilFile;

/**
 * Manually test echo cancel on a single machine:
 * 
 * Press enter to start recording "remote" sample
 * Press enter to finish recording "remote" sample
 * 
 * Program starts to play back "remote" sample in a loop.
 * 
 * Press enter to start recording "local" sample (while remote is being played locally)
 * Press enter to stop recording "local" sample
 * 
 * Program starts to play back what the remote will hear from the "local" sample.
 * 
 * @author rizsi
 *
 */
public class ManualTestEchoCancel2 {
	SpeexEchoCancel echo;
	Mic m;
	public static final int frameSamples=256;
	public static AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		// The platform default byte order
		boolean bigEndian = ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	
	public static void main(String[] args) throws Exception
	{
		new ManualTestEchoCancel2().run();
	}
	private void run() throws Exception
	{
		byte[] alapzaj=UtilFile.loadFile(new File("/home/rizsi/tmp/video/remote.sw"));
		AudioFormat format=getFormat();
		
		final Mixer mixer = AudioSystem.getMixer(null);
		m=new Mic(mixer, format, frameSamples);
		Play p=new Play(mixer, format, frameSamples);
		echo=new SpeexEchoCancel();
		echo.setLog(true);
		echo.setup(m, p, frameSamples);
		echo.start();
		p.start();
		m.start();
		RecordBoth r=new RecordBoth(echo, m);
		p.setSample(alapzaj);
		try(Scanner br=new Scanner(System.in))
		{
			System.out.println("Press ENTER to leave");
			br.nextLine();
			r.stop();
			p.setSample(r.echoCancelled.toByteArray());
			UtilFile.saveAsFile(new File("/tmp/rcancelled.sw"), r.echoCancelled.toByteArray());
			br.nextLine();
		}
		System.exit(0);
	}
}
