package com.rizsi.rcom.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UtilStream {
	public static void readFully(byte[] bs, InputStream i) throws IOException {
		readFully(bs, i, bs.length);
	}
	public static void readFully(byte[] bs, InputStream i, int k) throws IOException {
		int at=0;
		while(k>0)
		{
			int n=i.read(bs, at, k);
			if(n<=0)
			{
				throw new EOFException("Data read returned: "+n);
			}
			at+=n;
			k-=n;
		}
	}
	public static void pipeToFully(InputStream is, int len, byte[] buffer, OutputStream os) throws IOException
	{
		boolean err=false;
		while(len>0)
		{
			int n=Math.min(len, buffer.length);
			int nbytes=is.read(buffer, 0, n);
			if(nbytes<0)
			{
				throw new EOFException();
			}
			len-=nbytes;
			if(!err)
			{
				try
				{
					os.write(buffer, 0, nbytes);
				}catch(Exception e){
					// Ignore errors
					err=true;
				}
			}
		}
		try
		{
			os.flush();
		}catch(Exception e){// Ignore errors
		}
	}
}
