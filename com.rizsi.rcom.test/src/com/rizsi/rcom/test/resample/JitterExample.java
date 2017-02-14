package com.rizsi.rcom.test.resample;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.rizsi.rcom.audio.JitterResampler;
import com.rizsi.rcom.audio.SpeexResampler;
import com.rizsi.rcom.test.echocancel.ManualTestEchoCancel;

import hu.qgears.commons.UtilFile;

public class JitterExample {
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
			try(JitterResampler rs=new JitterResampler(8000, framesamples, 2))
			{
				new FeedThread(lis, rs).start();
				final byte[] buffer=new byte[framesamples*2];;
				while(true)
				{
					rs.readOutput(buffer);
					s.write(buffer, 0, buffer.length);
				}
			}
		}
	}
	private static class FeedThread extends Thread
	{
		InputStream is;
		JitterResampler rs;
		
		public FeedThread(InputStream is, JitterResampler rs) {
			super();
			this.is = is;
			this.rs = rs;
		}
		byte[] buffer=new byte[framesamples*2];
		long t;
		@Override
		public void run() {
			try {
				t=System.nanoTime();
				while(true)
				{
					readone();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void readone() throws Exception {
			is.read(buffer);
			rs.writeInput(buffer);
			long fedTime=1000l*1000*1000*framesamples/8000;
			t=t+fedTime;
			long diff=t-System.nanoTime();
//			System.out.println("Wait: "+diff+" fedtime: "+fedTime);
			if(diff>0)
			{
				TimeUnit.NANOSECONDS.sleep(diff);
			}
		}
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
			s.write(data, 0, nsamples*2);
		}
		
	}
}
