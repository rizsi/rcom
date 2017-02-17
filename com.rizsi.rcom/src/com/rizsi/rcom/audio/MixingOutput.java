package com.rizsi.rcom.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

/**
 * Real-time mix a list of sources into a single output.
 * values below linearLimit are unaltered
 * values over linearLimit are mapped to the range [linearLimit, MAX_SHORT]
 * @author rizsi
 *
 */
abstract public class MixingOutput extends Thread
{
	private boolean exit;
	private List<ISyncAudioSource> sources=new ArrayList<ISyncAudioSource>();
	private List<ISyncAudioSource> toAdd=new ArrayList<>();
	private List<ISyncAudioSource> toRemove=new ArrayList<>();
	public static int linearLimit=28000;
	public static int negLinearLimit=-linearLimit;
	public MixingOutput()
	{
		super("Audio mixer output");
	}
	public void run()
	{
		try {
			int nbyte=openAudioOutput();
			try
			{
				byte[] buffer=new byte[nbyte];
				ShortBuffer asShort=ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder()).asShortBuffer();
				int n=nbyte/2;
				IntBuffer mixing=IntBuffer.allocate(n);
				startAudioOutput();
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
						if(resampler.isClosed())
						{
							removeResampler(resampler);
						}
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
					writeAudioOutput(buffer);
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
			} finally
			{
				closeAudioOutput();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	abstract protected void closeAudioOutput();
	abstract protected void writeAudioOutput(byte[] buffer);
	abstract protected int openAudioOutput() throws LineUnavailableException;
	abstract protected void startAudioOutput();
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
