package com.rizsi.rcom.test.resample;

import java.io.IOException;
import java.io.InputStream;

public class LoopInputStream extends InputStream {
	private byte[] data;
	private int at;
	public LoopInputStream(byte[] data) {
		super();
		this.data = data;
	}
	@Override
	public int read() throws IOException {
		int i=at;
		at++;
		if(at>=data.length)
		{
			at=0;
		}
		return data[i];
	}
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int lenOrig=len;
		while(len>0)
		{
			int n=Math.min(len, data.length-at);
			System.arraycopy(data, at, b, off, n);
			off+=n;
			len-=n;
			at+=n;
			if(at>=data.length)
			{
				at=0;
			}
		}
		return lenOrig;
	}
}
