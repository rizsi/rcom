package com.rizsi.rcom;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class StreamSinkAudio extends StreamSinkSimplex
{
	StreamParameters p;
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	private AudioFormat format;
	private SourceDataLine line;
	public StreamSinkAudio(StreamParametersAudio p) {
		super(p.name);
		this.p=p;
	}
	@Override
	public void start() throws Exception
	{
	     format = StreamSourceAudio.getFormat();
//	      final AudioInputStream ais = 
//	        new AudioInputStream(input, format, 
//	        audio.length / format.getFrameSize());
	      DataLine.Info info = new DataLine.Info(
	        SourceDataLine.class, format);
	      line = (SourceDataLine)
	        AudioSystem.getLine(info);
	      line.open(format);
	      line.start();
	}
	@Override
	public void readFully(InputStream is, int len) throws IOException {
		while(len>0)
		{
			int n=Math.min(len, buffer.length);
			int nbytes=is.read(buffer, 0, n);
			if(nbytes<0)
			{
				throw new EOFException();
			}
			len-=nbytes;
			line.write(buffer, 0, nbytes);
		}
	}
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
