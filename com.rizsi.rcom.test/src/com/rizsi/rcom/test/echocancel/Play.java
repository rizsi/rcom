package com.rizsi.rcom.test.echocancel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class Play extends Thread
{
	SourceDataLine s;
	
	public Play(Mixer mixer, AudioFormat format, int framesamples) throws LineUnavailableException {
		DataLine.Info info2= new DataLine.Info(SourceDataLine.class, format);
		s=(SourceDataLine) mixer.getLine(info2);
		s.open(format, framesamples*2);
		System.out.println("Play buffer size: "+s.getBufferSize());
	}
	private byte[] sample;
	volatile private OutputStream speexCopy;
	private ByteArrayInputStream bis;
	byte[] buffer;
	private boolean logged=true;
	@Override
	public void run() {
		buffer=new byte[s.getBufferSize()];
		s.start();
		int frameindex=0;
		try {
			while(true)
			{
				synchronized (this) {
					if(sample!=null)
					{
						if(!logged)
						{
							System.out.println("Sample starts at: "+frameindex);
							logged=true;
						}
						int n=bis.read(buffer, 0, buffer.length);
						if(n<buffer.length)
						{
							if(n<0)
							{
								n=0;
							}
							switchBuffer();
							if(sample!=null)
							{
								bis=new ByteArrayInputStream(sample);
								bis.read(buffer, n, buffer.length-n);
							}else
							{
								for(int i=0;i<buffer.length;++i)
								{
									buffer[i]=0;
								}
							}
						}
					}
				}
				s.write(buffer, 0, buffer.length);
				if(speexCopy!=null)
				{
					speexCopy.write(buffer);
				}
				frameFinished(frameindex);
				frameindex++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected void switchBuffer() {
		// TODO Auto-generated method stub
		
	}
	protected void frameFinished(int frameindex) {
		// TODO Auto-generated method stub
		
	}
	synchronized public void setSample(byte[] sample)
	{
		this.sample=sample;
		if(sample!=null)
		{
			this.bis=new ByteArrayInputStream(sample);
		}
		else
		{
			bis=null;
			for(int i=0;i<buffer.length;++i)
			{
				this.buffer[i]=0;
			}
		}
	}
	public byte[] getSample() {
		return sample;
	}
	public void setSpeexCopy(OutputStream speexCopy) {
		this.speexCopy = speexCopy;
	}
	public void setLogged(boolean b) {
		logged=b;
	}
}