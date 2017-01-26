package com.rizsi.rcom.webcam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Parse ffmpeg error stream to find out the timestamp of the first frame.
 * @author rizsi
 *
 */
public class FFmpegOutputParser {

	public double process(Reader in) throws IOException {
		int maxlines=1000;
		BufferedReader br=new BufferedReader(in);
		String line;
		boolean next=false;
		int i=0;
		while((line=br.readLine())!=null)
		{
			if(next)
			{
				String pattern=" start: ";
				int idx=line.indexOf(pattern);
				int idx2=line.indexOf(",", idx+pattern.length());
				String startS=line.substring(idx+pattern.length(), idx2);
				double t0=Double.parseDouble(startS);
				return t0;
			}
			if(line.startsWith("Input #0"))
			{
				next=true;
			}
			i++;
			if(i>maxlines)
			{
				throw new IOException("Pattern not found");
			}
		}
		throw new IOException("Pattern not found");
	}

}
