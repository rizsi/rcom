package com.rizsi.rcom.test.echocancel;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MeasurePower extends Thread
{
	int bufferSize;
	PipedInputStream rec=new PipedInputStream();
	public final PipedOutputStream recSink=new PipedOutputStream();
	
	public MeasurePower(int frameSamples, Mic m) throws IOException {
		super();
		bufferSize=frameSamples*2;
		rec.connect(recSink);
		m.setSpeexCopy(recSink);
	}

	@Override
	public void run()
	{
		byte [] input=new byte[bufferSize];
		ByteBuffer bb=ByteBuffer.wrap(input).order(ByteOrder.nativeOrder());
		try {
			int frameindex=0;
			int limit=1000000000;
			while(true)
			{
				readFully(rec, input);
				bb.clear();
				bb.limit(bb.capacity());
				long sum=0;
				int n=0;
				while(bb.hasRemaining())
				{
					short value=bb.getShort();
					sum+=value*value;
					n++;
				}
				if(sum>limit)
				{
					System.out.println("SUM: "+sum+" samples: "+n+" frameindex: "+frameindex);
				}
				frameindex++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void readFully(InputStream rec, byte[] input) throws IOException {
		int at=0;
		while(at<input.length)
		{
			int n=rec.read(input, at, input.length-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
}