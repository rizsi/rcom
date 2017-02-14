package com.rizsi.rcom.audio;

import java.io.IOException;

public class Pipe {
	final private byte[] buffer;
	private int writeAt;
	private int readAt;
	private int available;
	public Pipe(int i) {
		buffer=new byte[i];
	}

	public void write(byte[] bs, int at, int len) throws IOException {
		while(len>0)
		{
			int n=Math.min(buffer.length-available, buffer.length-writeAt);
			n=Math.min(n, len);
			if(n==0)
			{
				throw new IOException("Buffer full");
			}
			System.arraycopy(bs, at, buffer, writeAt, n);
			writeAt+=n;
			if(writeAt>=buffer.length)
			{
				writeAt-=buffer.length;
			}
			len-=n;
			at+=n;
			available+=n;
		}
	}

	public int available() {
		return available;
	}

	public void readAhead(byte[] bs, int at, int len) throws IOException {
		if(len>available)
		{
			throw new IOException("Buffer underflow");
		}
		int readAt=this.readAt;
		while(len>0)
		{
			int n=Math.min(buffer.length-readAt, len);
			System.arraycopy(buffer, readAt, bs, at, n);
			readAt+=n;
			if(readAt>=buffer.length)
			{
				readAt-=buffer.length;
			}
			len-=n;
			at+=n;
		}
	}

	public void read(int len) {
		available-=len;
	}

	public int read(byte[] data, int at, int i) throws IOException {
		readAhead(data, at, i);
		read(i);
		return i;
	}

}
