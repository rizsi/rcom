package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.SourceDataLine;

public class SourceDataLineOutputStream extends OutputStream
{
	final private SourceDataLine sdl;
	final private byte[] single=new byte[1];
	
	public SourceDataLineOutputStream(SourceDataLine sdl) {
		super();
		this.sdl = sdl;
	}
	@Override
	public void write(int b) throws IOException {
		sdl.write(single, 0, 1);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		while(len>0)
		{
			int n=sdl.write(b, off, len);
			len-=n;
			off+=n;
		}
	}

}
