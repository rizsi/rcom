package com.rizsi.rcom;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.rizsi.rcom.util.UtilStream;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.coolrmi.streams.IConnection;

public class DemuxedConnection implements IConnection, IChannelReader
{
	public static final int bufferSize=8192;
	private PipedInputStream pipeIn=new PipedInputStream();
	private PipedOutputStream pipeInWriter=new PipedOutputStream();
	private OutputStream os;
	private ISocket s;
	private byte[] buffer=new byte[bufferSize];
	private ChannelMultiplexer multiplexer;
	public final SignalFutureWrapper<DemuxedConnection> connectionclosed=new SignalFutureWrapper<>();
	public String serverId=VideoServerTCPListener.serverID;
	public String clientId=VideoServerTCPListener.clientID;
	public String userName;
	byte[] b=new byte[1024];
	ByteBuffer bb=ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
	public DemuxedConnection(ISocket s, boolean server) throws IOException {
		this.s=s;
		pipeInWriter.connect(pipeIn);
		OutputStream o=s.getOutputStream();
		final InputStream is=s.getInputStream();
		if(server)
		{
			o.write(serverId.getBytes(StandardCharsets.UTF_8));
			boolean commandProcesed=true;
			while(commandProcesed)
			{
				commandProcesed=false;
				byte[] req=clientId.getBytes(StandardCharsets.UTF_8);
				readFully(b, is, req.length);
				assertEq(req, b);
				int command=is.read();
				switch(command)
				{
				case 'u':
					// Specify user
					readFully(b, is, 4);
					bb.clear();
					int l=bb.getInt();
					readFully(b, is, l);
					userName=new String(b,0,l);
					commandProcesed=true;
					break;
				}
			}
		}else
		{
			o.write(clientId.getBytes(StandardCharsets.UTF_8));
			o.write('0');
			byte[] req=serverId.getBytes(StandardCharsets.UTF_8);
			readFully(b, is, req.length);
			assertEq(req, b);
		}
		multiplexer=new ChannelMultiplexer(o);
		os=new BufferedOutputStream(multiplexer.createStream());
		multiplexer.addListener(0, this);
		Thread t=new Thread("Channel demux"){
			@Override
			public void run() {
				try {
					multiplexer.readThread(is);
				}catch(EOFException e)
				{
					// EOF is normal closing of the channel.
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
	private void assertEq(byte[] req, byte[] b2) throws IOException {
		for(int i=0;i<req.length;++i)
		{
			if(req[i]!=b2[i])
			{
				throw new IOException();
			}
		}
	}
	private void readFully(byte[] bs, InputStream i, int k) throws IOException {
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

	@Override
	public InputStream getInputStream() throws IOException {
		return pipeIn;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public void close() throws IOException {
		connectionclosed.ready(this, null);
		s.close();
	}

	@Override
	public void readFully(InputStream is, int len) throws IOException {
		UtilStream.pipeToFully(is, len, buffer, pipeInWriter);
	}

	public ChannelMultiplexer getMultiplexer() {
		return multiplexer;
	}
}
