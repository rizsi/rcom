package com.rizsi.rcom;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Multiplex several channels into a single channel.
 * @author rizsi
 */
public class ChannelMultiplexer {
	private OutputStream output;
	private byte[] headerBytes=new byte[8];
	private ByteBuffer header=ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN);
	private byte[] readHeaderBytes=new byte[8];
	private ByteBuffer readHeader=ByteBuffer.wrap(readHeaderBytes).order(ByteOrder.LITTLE_ENDIAN);
	private int nChannel=0;
	private Map<Integer, IChannelReader> readers=Collections.synchronizedMap(new HashMap<>());
	public class ChannelOutputStream extends OutputStream
	{
		private int channel;
		private volatile boolean closed;
		public ChannelOutputStream(int channel) {
			super();
			this.channel = channel;
		}
		@Override
		public void write(int b) throws IOException {
			synchronized (output) {
				writeHeader(channel, 1);
				output.write(b);
			}
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (output) {
				writeHeader(channel, len);
				output.write(b, off, len);
			}
		}
		@Override
		public void write(byte[] b) throws IOException {
			synchronized (output) {
				writeHeader(channel, b.length);
				output.write(b);
			}
		}
		@Override
		public void flush() throws IOException {
			synchronized (output) {
				output.flush();
			}
		}
		private void writeHeader(int channel, int len) throws IOException {
			if(closed)
			{
				throw new RuntimeException("Closed");
			}
			header.clear();
			header.putInt(channel);
			header.putInt(len);
			output.write(headerBytes);
		}
		public int getChannel() {
			return channel;
		}
		@Override
		public void close() throws IOException {
			synchronized (output) {
				closed=true;
			}
		}
	}
	
	public ChannelMultiplexer(OutputStream output) {
		super();
		this.output = output;
	}
	public ChannelOutputStream createStream()
	{
		synchronized (output) {
			return new ChannelOutputStream(nChannel++);
		}
	}
	public void readThread(InputStream is) throws IOException
	{
		while(true)
		{
			readFully(is, readHeaderBytes);
			readHeader.clear();
			int channel=readHeader.getInt();
			int len=readHeader.getInt();
			IChannelReader r=readers.get(channel);
			if(r!=null)
			{
				r.readFully(is, len);
			}else
			{
				// Data is thrown to /dev/null in case there is no listener registered.
				readFully(is, len);
			}
		}
	}
	private void readFully(InputStream is, int len) throws IOException {
		for(int i=0;i<len;++i)
		{
			is.read();
		}
	}
	private void readFully(InputStream is, byte[] tg) throws IOException {
		int off=0;
		while(off<tg.length)
		{
			int n=is.read(tg, off, tg.length-off);
			if(n<0)
			{
				throw new EOFException();
			}
			off+=n;
		}
	}
	public void addListener(int channel, IChannelReader r)
	{
		readers.put(channel, r);
	}
}
