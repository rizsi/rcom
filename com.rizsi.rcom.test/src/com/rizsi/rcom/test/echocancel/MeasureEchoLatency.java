package com.rizsi.rcom.test.echocancel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.rizsi.rcom.audio.Mic;
import com.rizsi.rcom.audio.Play;

/**
 * Measure echo latency.
 * 
 * Also measure synchronity of input and output: 83576 frames were measured without diverging
 * 
 * @author rizsi
 *
 */
public class MeasureEchoLatency {
	SourceDataLine s;
	TargetDataLine t;
	Mic m;
	class Mic2 extends Mic
	{
		
		public Mic2(Mixer mixer, AudioFormat format, int frameSamples) throws LineUnavailableException {
			super(mixer, format, frameSamples);
		}

		@Override
		protected void frameFinished(int frameindex) {
//			if(frameindex%10==0)
//			{
//				System.out.println("Rec frame: "+frameindex);
//			}
		}
	}
	class Play2 extends Play
	{
		public Play2(Mixer mixer, AudioFormat format, int framesamples) throws LineUnavailableException {
			super(mixer, format, framesamples);
		}

		@Override
		protected void frameFinished(int frameindex) {
//			if(frameindex%10==0)
//			{
//				System.out.println("Play frame: "+frameindex);
//			}
		}
	}
	public static void main(String[] args) throws Exception
	{
		new MeasureEchoLatency().run();
	}
	private void run() throws Exception
	{
		int frameSize=256;
		AudioFormat format=ManualTestEchoCancel.getFormat();
		
		final Mixer mixer = AudioSystem.getMixer(null);
		m=new Mic2(mixer, format, ManualTestEchoCancel.frameSamples);
		MeasurePower mp=new MeasurePower(frameSize, m);
		mp.start();
		Play p=new Play2(mixer, format, ManualTestEchoCancel.frameSamples);
		p.start();
		m.start();
		byte[] sample=new byte[320000];
		ByteBuffer s=ByteBuffer.wrap(sample);
		s.order(ByteOrder.nativeOrder());
		for(int i=0;i<500;++i)
		{
			s.putShort((short)(32000*Math.sin(0.3*i)));
		}
		try(Scanner br=new Scanner(System.in))
		{
			System.out.println("Press ENTER to start sample");
			br.nextLine();
			while(true)
			{
				Thread.sleep(1000);
				p.setSample(sample);
				Thread.sleep(1000);
				p.setSample(null);
				p.setLogged(false);
			}
//			br.nextLine();
		}
//		System.exit(0);
	}
}
