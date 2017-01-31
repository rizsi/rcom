package com.rizsi.rcom.test.nio.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

import com.rizsi.rcom.test.nio.MultiplexerReceiver;

/**
 * Multiplexer receiver that buffers the incoming data and it is possible to read the
 * incoming data from a stream.
 * 
 * This implementation can block the main NIO thread in case the receiving buffer is full!
 * 
 * @author rizsi
 *
 */
public class InputStreamReceiver extends MultiplexerReceiver
{
	private ByteBuffer bb;
	private ByteBuffer reader;
	private long nWrite;
	private long nRead;
	public InputStream in;
	class Is extends InputStream
	{
		@Override
		public int read() throws IOException {
			try {
				synchronized (bb) {
					int cap=(int)(nWrite-nRead);
					while(cap==0)
					{
						bb.wait();
						cap=(int)(nWrite-nRead);
					}
					nRead++;
					// TODO reading one by one is not optimal
					byte ret=reader.get();
					if(!reader.hasRemaining())
					{
						reader.clear();
					}
					return 0xff&ret;
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}
	@Override
	public int read(SelectionKey key, ReadableByteChannel bc, int remainingBytes) throws IOException {
		int n;
		try {
			synchronized (bb) {
				boolean needNotify=nRead==nWrite;
				int spaceInBuffer=getPositiveSpaceInBuffer();
				int l=Math.min(remainingBytes, bb.capacity()-bb.position());
				l=Math.min(l, spaceInBuffer);
				bb.limit(l+bb.position());
				n=bc.read(bb);
				if(n>0)
				{
					nWrite+=n;
					if(needNotify)
					{
						bb.notifyAll();
					}
				}
				if(bb.position()==bb.capacity())
				{
					bb.clear();
				}
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return n;
	}
	private int getPositiveSpaceInBuffer() throws InterruptedException {
		int cap=(int)(bb.capacity()-(nWrite-nRead));
		while(cap==0)
		{
			bb.wait();
			cap=(int)(bb.capacity()-(nWrite-nRead));
		}
		return cap;
	}
	public InputStreamReceiver(int pipeSize) throws IOException {
		super();
		bb=ByteBuffer.allocateDirect(pipeSize);
		reader=bb.asReadOnlyBuffer();
		in=new Is();
	}

}
