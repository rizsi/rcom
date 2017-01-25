package com.rizsi.rcom.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class UtilStream {
	public static void readFully(byte[] bs, InputStream i, int k) throws IOException {
		int at=0;
		while(at<k)
		{
			int n=i.read(bs, at, k-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}

}
