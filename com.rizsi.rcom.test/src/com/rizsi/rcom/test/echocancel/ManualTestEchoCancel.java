package com.rizsi.rcom.test.echocancel;

import java.nio.ByteOrder;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import com.rizsi.rcom.audio.Mic;
import com.rizsi.rcom.audio.Play;
import com.rizsi.rcom.audio.SpeexEchoCancel;

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
public class ManualTestEchoCancel {
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
		new ManualTestEchoCancel().run();
	}
	private void run() throws Exception
	{
		AudioFormat format=getFormat();
		
		final Mixer mixer = AudioSystem.getMixer(null);
//		try {
//			mixer.synchronize(new Line[]{t, s}, true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			// e.printStackTrace();
//		}
		m=new Mic(mixer, format, frameSamples);
		Play p=new Play(mixer, format, frameSamples);
		echo=new SpeexEchoCancel();
		echo.setLog(true);
		echo.setup(m, p, frameSamples);
		echo.start();
		p.start();
		m.start();
		try(Scanner br=new Scanner(System.in))
		{
			System.out.println("Press ENTER to start recording sample");
			br.nextLine();
			RecordBoth record=new RecordBoth(echo, m);
			System.out.println("Recording sample...");
			br.nextLine();
			record.stop();
			p.setSample(record.echoCancelled.toByteArray());
			System.out.println("Sample recording stopped");
			System.out.println("Press ENTER to start recording echo sample");
			br.nextLine();
			record=new RecordBoth(echo, m);
			System.out.println("Echo sample recording...");
			br.nextLine();
			record.stop();
			p.setSample(record.echoCancelled.toByteArray());
			System.out.println("Echo sample recorded");
			System.out.println("replaying echo cancelled sample");
			System.out.println("Press ENTER to switch to echo version");
			br.nextLine();
			p.setSample(record.raw.toByteArray());
			System.out.println("Echo version");
			System.out.println("Press ENTER to leave");
			br.nextLine();
		}
		System.exit(0);
	}
}
