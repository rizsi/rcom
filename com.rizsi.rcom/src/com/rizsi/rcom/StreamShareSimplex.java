package com.rizsi.rcom;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;

public class StreamShareSimplex extends StreamShare {
	class Reg implements StreamRegistration
	{
		ChannelOutputStream cos;
		
		public Reg(ChannelOutputStream cos) {
			super();
			this.cos = cos;
		}

		@Override
		public void close() {
			try {
				cos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public IStreamData getData() {
			return new StreamDataSimplex(cos.getChannel());
		}

		@Override
		public void launch() {
			// Simplex streams are launched at once without waiting for the client to actually listen to the channel.
		}
	}
	private List<Reg> clients=new ArrayList<>();
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	private int channel;
	public StreamShareSimplex(VideoConnection videoConnection, int channel, StreamParameters params) {
		super(videoConnection, params);
		this.channel=channel;
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
			writeAll(buffer, 0, nbytes);
		}
		flushAll();
	}

	private void flushAll() {
		for(int i=0;i<clients.size();++i)
		{
			ChannelOutputStream cos=clients.get(i).cos;
			try {
				cos.flush();
			} catch (IOException e) {
				e.printStackTrace();
				clients.remove(i);
				i--;
			}
		}
	}

	private void writeAll(byte[] buffer, int offset, int nbytes) {
		for(int i=0;i<clients.size();++i)
		{
			ChannelOutputStream cos=clients.get(i).cos;
			try {
				cos.write(buffer, offset, nbytes);
			} catch (IOException e) {
				e.printStackTrace();
				clients.remove(i);
				i--;
			}
		}
	}

	public void dispose() {
		for(Reg r: clients)
		{
			r.close();
		}
		clients.clear();
	}

	@Override
	public StreamRegistration registerClient(VideoConnection videoConnection, int channel) {
		if(channel!=-1)
		{
			throw new IllegalArgumentException();
		}
		ChannelOutputStream cos=videoConnection.getConnection().getMultiplexer().createStream();
		Reg ret=new Reg(cos);
		clients.add(ret);
		return ret;
	}

	@Override
	public IStreamData getStreamData() {
		return new StreamDataSimplex(channel);
	}

}
