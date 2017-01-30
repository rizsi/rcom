package com.rizsi.rcom.test.echocancel;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class Mic extends Thread
{
	TargetDataLine t;
	
	public Mic(Mixer mixer, AudioFormat format, int frameSamples) throws LineUnavailableException {
		super();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		t=(TargetDataLine) mixer.getLine(info);
		t.open(format, frameSamples);
		System.out.println("Bytebuffer t: "+t.getBufferSize());
	}

	volatile private OutputStream os;
	volatile private OutputStream speexCopy;
	@Override
	public void run() {
		byte[] buffer=new byte[t.getBufferSize()];
		t.start();
		int frameindex=0;
		try {
			while(true)
			{
				int n=t.read(buffer, 0, buffer.length);
				if(n!=buffer.length)
				{
					throw new IOException("Not whole buffer read: "+n);
				}
				if(os!=null)
				{
					os.write(buffer);
				}
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

	protected void frameFinished(int frameindex) {
		// TODO Auto-generated method stub
		
	}

	public void setRecord(OutputStream record) {
		this.os=record;
	}
	public void setSpeexCopy(OutputStream speexCopy) {
		this.speexCopy = speexCopy;
	}
}