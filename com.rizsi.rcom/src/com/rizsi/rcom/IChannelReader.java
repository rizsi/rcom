package com.rizsi.rcom;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IChannelReader {

	/**
	 * The listener must read ecaxtly length bytes from the stream.
	 * @param is
	 * @param len
	 * @throws IOException 
	 */
	void readFully(InputStream is, int len) throws IOException;

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
