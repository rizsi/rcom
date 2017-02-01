package com.rizsi.rcom.webcam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

abstract public class FFmpegCaptureParser {
	public void process(Reader in) throws IOException
	{
		BufferedReader br=new BufferedReader(in);
		String line;
		int state=0;
		while((line=br.readLine())!=null)
		{
			switch (state) {
			case 0:
				if(line.startsWith("Output #0"))
				{
					state=1;
				}
				break;
			case 1:
				if(line.startsWith("    Stream #0:0:"))
				{
					state=2;
					int idx=line.indexOf(" fps");
					if(idx>0)
					{
						int idx2=line.lastIndexOf(" ", idx-1);
						if(idx2>0)
						{
							String fpsS=line.substring(idx2+1, idx);
							setFps(Integer.parseInt(fpsS));
						}
					}
				}
				break;
			case 2:
				if(line.startsWith("frame="))
				{
					int at="frame=".length();
					while(!Character.isDigit(line.charAt(at)))
					{
						at++;
					}
					int idx2=line.indexOf(' ', at);
					String frameS=line.substring(at, idx2);
					frameReceived(Integer.parseInt(frameS));
				}
			default:
				break;
			}
		}
	}

	abstract protected void frameReceived(int parseInt);

	abstract protected void setFps(int parseInt);
}
