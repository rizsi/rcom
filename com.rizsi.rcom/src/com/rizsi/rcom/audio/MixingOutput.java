package com.rizsi.rcom.audio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Real-time mix a list of sources into a single output.
 * values below linearLimit are unaltered
 * values over linearLimit are mapped to the range [linearLimit, MAX_SHORT]
 * @author rizsi
 *
 */
public class MixingOutput {
	private boolean exit;
	private List<ISyncAudioSource> sources=new ArrayList<ISyncAudioSource>();
	private List<ISyncAudioSource> toAdd=new ArrayList<>();
	private List<ISyncAudioSource> toRemove=new ArrayList<>();
	public static int linearLimit=28000;
	public static int negLinearLimit=-linearLimit;
	void run() throws LineUnavailableException, IOException
	{
		AudioFormat format = StreamSourceAudio.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		try(SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info))
		{
			line.open(format, StreamSourceAudio.requestBufferSize);
			line.start();
			int n=line.getBufferSize();
			byte[] buffer=new byte[n];
			ShortBuffer asShort=ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder()).asShortBuffer();
			IntBuffer mixing=IntBuffer.allocate(n);
			while(!exit)
			{
				mixing.clear();
				for(int i=0;i<n;++i)
				{
					mixing.put(0);
				}
				mixing.flip();
				for(ISyncAudioSource resampler: sources)
				{
					resampler.readOutput(buffer);
					for(int i=0;i<n;++i)
					{
						int sum=mixing.get(i);
						sum+=asShort.get(i);
						mixing.put(i, sum);
					}
				}
				for(int i=0;i<n;++i)
				{
					int sum=mixing.get(i);
					short value=nonLinearResampling(sum);
					asShort.put(i, value);
				}
				line.write(buffer, 0, buffer.length);
				synchronized (this) {
					for(ISyncAudioSource src: toAdd)
					{
						sources.add(src);
					}
					toAdd.clear();
					for(ISyncAudioSource src: toRemove)
					{
						sources.remove(src);
					}
					toRemove.clear();
				}
			}
		}
	}
	public static short nonLinearResampling(int sum) {
		if(sum>linearLimit)
		{
			double mul=200;
			double x=sum-linearLimit-1;
			System.out.println("x: "+x);
			int v=(int)(Math.log((x/mul+1))*mul)+linearLimit+1;
			return (short)v;
		}else if(sum<-linearLimit)
		{
			double mul=200;
			double x=negLinearLimit-sum-1;
			System.out.println("x: "+x);
			int v=(int)(Math.log((x/mul+1))*mul)+linearLimit+1;
			return (short)-v;
		}else
		{
			return (short)sum;
		}
	}
	public void addResampler(ISyncAudioSource src)
	{
		synchronized (this) {
			toAdd.add(src);
		}
	}
	public void removeResampler(ISyncAudioSource src)
	{
		synchronized (this) {
			toRemove.add(src);
		}
	}
}
