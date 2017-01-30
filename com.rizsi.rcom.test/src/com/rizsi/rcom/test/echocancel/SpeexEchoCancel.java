package com.rizsi.rcom.test.echocancel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.rizsi.rcom.IChannelReader;
import com.rizsi.rcom.NullOutputStream;
import com.rizsi.rcom.util.TeeOutputStream;

import hu.qgears.commons.ConnectStreams;

public class SpeexEchoCancel extends Thread
{
	int bufferSize;
	PipedInputStream play;
	PipedInputStream rec;
	private final PipedOutputStream playSink=new PipedOutputStream();
	private final PipedOutputStream recSink=new PipedOutputStream();
	private volatile OutputStream processed=new NullOutputStream();
	OutputStream speexInputMic;
	OutputStream speexInputMonitor;
	InputStream speexOutput;
	private boolean log;
	int nLate=1;
	int nDump=10;
	
	public SpeexEchoCancel()
	{
		super();
	}
	public void setup(Mic m, Play player, int frameSamples) throws IOException {
		bufferSize=frameSamples*2;
		int pipeSize=(Math.abs(nLate)+100)*bufferSize;
		play=new PipedInputStream(pipeSize);
		play.connect(playSink);
		rec=new PipedInputStream(pipeSize);
		rec.connect(recSink);
		Process p;
		p=Runtime.getRuntime().exec("/home/rizsi/github/rcom/speexexample/a.out");
		speexInputMic=p.getOutputStream();
		speexInputMonitor=p.getOutputStream();
		speexOutput=p.getInputStream();
		if(log)
		{
			speexInputMic=new TeeOutputStream(new OutputStream[]{speexInputMic, new FileOutputStream("/tmp/mic.sw")});
			speexInputMonitor=new TeeOutputStream(new OutputStream[]{speexInputMonitor, new FileOutputStream("/tmp/monitor.sw")});
		}
		ConnectStreams.startStreamThread(p.getErrorStream(), System.err);
		player.setSpeexCopy(playSink);
		m.setSpeexCopy(recSink);
		System.out.println("Speex buffer size: "+bufferSize);
	}

	@Override
	public void run()
	{
		byte [] input=new byte[bufferSize];
		byte [] echo=new byte[bufferSize];
		byte [] proc=new byte[bufferSize];
		try {
			NullOutputStream nullout=new NullOutputStream();
			// Add latency to mic input because Java sometimes "hears" sound before it was played and it makes echo cancelling impossible.
			if(nLate>=0)
			{
				for(int i=0;i<nLate;++i)
				{
//					IChannelReader.pipeToFully(nullin, bufferSize, input, speexInputMic);
					IChannelReader.pipeToFully(play, bufferSize, echo, nullout);
//					IChannelReader.pipeToFully(speexOutput, bufferSize, proc, processed);
				}
			}else
			{
				for(int i=0;i<-nLate;++i)
				{
					IChannelReader.pipeToFully(rec, bufferSize, input, nullout);
//					IChannelReader.pipeToFully(nullin, bufferSize, echo, speexInputMonitor);
//					IChannelReader.pipeToFully(speexOutput, bufferSize, proc, processed);
				}
			}
			for(int i=0;i<nDump;++i)
			{
				IChannelReader.pipeToFully(rec, bufferSize, input, nullout);
				IChannelReader.pipeToFully(play, bufferSize, echo, nullout);
			}
			while(true)
			{
				IChannelReader.pipeToFully(rec, bufferSize, input, speexInputMic);
				IChannelReader.pipeToFully(play, bufferSize, echo, speexInputMonitor);
				IChannelReader.pipeToFully(speexOutput, bufferSize, proc, processed);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setProcessed(OutputStream processed) {
		this.processed = processed;
	}
	public void setLog(boolean b) {
		log=b;
	}
}