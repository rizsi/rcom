package com.rizsi.rcom.util;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
	OutputStream[] children;
	
	public TeeOutputStream(OutputStream[] children) {
		super();
		this.children = children;
	}
	@Override
	public void write(int b) throws IOException {
		for(OutputStream os: children)
		{
			os.write(b);
		}
	}
	@Override
	public void write(byte[] b) throws IOException {
		for(OutputStream os: children)
		{
			os.write(b);
		}
	}
	@Override
	public void flush() throws IOException {
		for(OutputStream os: children)
		{
			os.flush();
		}
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for(OutputStream os: children)
		{
			os.write(b, off, len);
		}
	}
	@Override
	public void close() throws IOException {
		for(OutputStream os: children)
		{
			os.close();
		}
	}
	

}
