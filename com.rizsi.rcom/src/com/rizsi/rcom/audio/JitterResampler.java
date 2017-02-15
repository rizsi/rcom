package com.rizsi.rcom.audio;

import java.io.IOException;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.audio.SpeexResampler.ResampledReceiver;
import com.rizsi.rcom.util.MyAssert;

public class JitterResampler implements AutoCloseable, ResampledReceiver {
	private int bytesPerSecond;
	private int sampleRate;
	private int framesamples;
	/**
	 * Number of received bytes.
	 */
	private long received;
	/**
	 * Number of resampled bytes. (The received bytes transformed by the resampler)
	 */
	private long resampled;
	/**
	 * Number of used bytes (used by the playback).
	 */
	private long used;
	private long sec=0;
	/**
	 * This is the measured rate of input and output speed: input/output (received/used).
	 */
	private double rate=1.0;
	/**
	 * The processing rate required by the jitter buffer to use to control the jitter buffer length.
	 */
	private double reqRate=1.0;
	/**
	 * This is the targeted length of the buffered samples in seconds.
	 */
	private double bufferLengthTarget;
	private SpeexResampler resampler;
	private Pipe pis;
	private Pipe pos;
	public JitterResampler(AbstractRcomArgs args, int sampleRate, int framesamples, int sampleSizeInBytes) throws Exception
	{
		MyAssert.myAssert(sampleSizeInBytes==2);
		this.sampleRate=sampleRate;
		bufferLengthTarget=((double)framesamples)/sampleRate*3;
		bytesPerSecond=sampleSizeInBytes*sampleRate;
		this.framesamples=framesamples;
		resampler=new SpeexResampler(args, framesamples, this);
		// 5 seconds of buffer
		pis=pos=new Pipe(sampleRate*sampleSizeInBytes*5);
	}
	/**
	 * Write input data to the resampler.
	 * @param data
	 * @throws IOException
	 */
	synchronized public void writeInput(byte[] data) throws Exception
	{
		received+=data.length;
		int sourceRate=(int)(reqRate*sampleRate);
		resampler.feed(data, sourceRate, sampleRate);
	}
	private void updateReqRate() {
		// Update rate
		double ratemeasurement=((double)received/used);
		double rateConvergeSpeed=0.001;
		rate=rate*(1.0-rateConvergeSpeed)+ratemeasurement*rateConvergeSpeed;
		double diff=getBufferLengthInSecs()-bufferLengthTarget;
		// Assume we have 1 second of buffered data then we speed up the playack rate with 1/regulateTime
		// Then we use the buffered data in regulateTime seconds.
		// If diff is negative - buffered samples are not enough then we control playback speed much more agressive
		double regulateTime=diff>0?10:.5;
		reqRate=rate+diff/regulateTime;
		if(reqRate<0.2) reqRate=0.2;
		if(reqRate>3) reqRate=3;
	}
	@Override
	public void receiveResampled(byte[] data, int nsamples) throws IOException {
		MyAssert.myAssert(nsamples>0);
		MyAssert.myAssert(nsamples<=framesamples);
		MyAssert.myAssert(data.length>=nsamples*2);
		pos.write(data, 0, nsamples*2);
		resampled+=nsamples*2;
	}
	synchronized public void readOutput(byte[] data) throws IOException
	{
		while(received<bufferLengthTarget*bytesPerSecond)
		{
			if(received>0)
			{
				// TODO would be better to initialize the counters when playback is started with "good" values.
				System.out.println("Buffering... "+received);
				used+=data.length;
				resampled+=data.length;
			}else
			{
				System.out.println("Read data from empty channel.");
			}
			for(int i=0;i<data.length; ++i)
			{
				data[i]=0;
			}
			return;
		}
		int at=0;
		used+=data.length;
		long missing=Math.max(0, used-resampled);
		if(missing<data.length)
		{
			at+=pis.read(data, at, (int)(data.length-missing));
		}
		if(missing>0)
		{
			// System.out.println("Samples missing: "+missing+" at second: "+sec);
			// Add silence - We don't have enough samples
			resampled+=missing;
			for(int i=at;i<data.length; ++i)
			{
				data[i]=0;
			}
		}
		long currsec=used/bytesPerSecond;
		if(currsec!=sec)
		{
			updateReqRate();
			// System.out.println("Current time: "+sec+" Buffer length: "+getBufferLengthInSecs()+" rate: "+rate+" reqRate: "+reqRate+" target: "+bufferLengthTarget);
		}
		sec=currsec;
	}
	private double getBufferLengthInSecs()
	{
		return ((double)(resampled-used))/bytesPerSecond;
	}
	@Override
	public void close() {
		if(resampler!=null)
		{
			resampler.close();
		}
	}
}
