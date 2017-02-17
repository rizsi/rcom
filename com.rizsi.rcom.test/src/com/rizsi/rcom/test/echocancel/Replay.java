package com.rizsi.rcom.test.echocancel;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import hu.qgears.commons.UtilFile;

public class Replay {
	public static void main(String[] args) throws IOException, LineUnavailableException {
		File folder=new File("/home/rizsi/tmp/video");
		byte[] data=UtilFile.loadFile(new File(folder, "remote.sw"));
		byte[] data2=UtilFile.loadFile(new File(folder, "local.sw"));
		System.out.println("remote.sw max: "+measureMax(data));
		System.out.println("local.sw max: "+measureMax(data2));
		byte[] data3=sum(data, data2);
		UtilFile.saveAsFile(new File(folder, "rawmic.sw"), data3);
		AudioFormat format=ManualTestEchoCancel.getFormat();
		final Mixer mixer = AudioSystem.getMixer(null);
		Play p=new Play(mixer, format, ManualTestEchoCancel.frameSamples)
		{
			@Override
			protected void switchBuffer() {
				if(getSample()==data)
				{
					setSample(data2);
				}else if(getSample()==data2)
				{
					setSample(data3);
				}
			}
		};
		p.start();
		p.setSample(data);
	}
	public static byte[] sum(byte[] data, byte[] data2)
	{
		ByteBuffer w1=ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		ByteBuffer w2=ByteBuffer.wrap(data2).order(ByteOrder.nativeOrder());
		byte[] sum=new byte[Math.max(data.length, data2.length)];
		ByteBuffer dst=ByteBuffer.wrap(sum).order(ByteOrder.nativeOrder());
		int n=sum.length/2;
		int firstW1Sample=n-data.length/2;
		int firstW2Sample=n-data2.length/2;
		for(int i=0;i<n;++i)
		{
			short sample1=0;
			if(i>=firstW1Sample)
			{
				sample1=w1.getShort();
			}
			short sample2=0;
			if(i>=firstW2Sample)
			{
				sample2=w2.getShort();
			}
			dst.putShort((short)(sample2+sample1));
		}
		return sum;
	}
	public static int measureMax(byte[] data)
	{
		int max=0;
		ByteBuffer wrap=ByteBuffer.wrap(data);
		wrap.order(ByteOrder.nativeOrder());
		for(int i=0;i<data.length/2;++i)
		{
			short sample=wrap.getShort();
			max=Math.max(Math.abs(sample), max);
		}
		return max;
	}
}
