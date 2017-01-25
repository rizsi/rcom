package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.commons.signal.Slot;

public class StreamShareVNC extends StreamShare {
	class Reg implements StreamRegistration, IChannelReader
	{
		ChannelOutputStream cos;
		private int clientChannel;
		private OutputStream os;
		private InputStream is;
		private byte[] buffer2=new byte[DemuxedConnection.bufferSize];
		
		public Reg() {
			super();
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
			return new StreamDataDuplex(clientChannel, cos.getChannel());
		}

		public void setChannels(VideoConnection videoConnection, Socket s, int clientChannel, ChannelOutputStream cos) throws IOException {
			this.clientChannel=clientChannel;
			this.cos=cos;
			os=s.getOutputStream();
			is=s.getInputStream();
			videoConnection.getConnection().getMultiplexer().addListener(clientChannel, this);
		}

		@Override
		public void readFully(InputStream is, int len) throws IOException {
			IChannelReader.pipeToFully(is, len, buffer2, os);
		}

		@Override
		public void launch() {
			UtilProcess.streamErrorOfProcess(is, cos);
		}
	}
	private List<Reg> clients=new ArrayList<>();
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	private int channel;
	private ChannelOutputStream back;
	private Socket s;
	private OutputStream os;
	private int port=9998;
	private SignalFutureWrapper<Integer> processresult;
	Process p;
	public StreamShareVNC(VideoConnection videoConnection, int channel, StreamParameters params) {
		super(videoConnection, params);
		this.channel=channel;
		back=videoConnection.getConnection().getMultiplexer().createStream();
		int n=5;
		int localport=5900+n;
		try {
			ServerSocket ss=new ServerSocket();
			try
			{
				ss.bind(new InetSocketAddress("localhost", localport));
				String command="x11vnc -reflect localhost:"+n+" -forever -rfbport "+port+" -localhost";
				p=Runtime.getRuntime().exec(command);
				processresult=UtilProcess.getProcessReturnValueFuture(p);
				processresult.addOnReadyHandler(new Slot<SignalFuture<Integer>>() {
					
					@Override
					public void signal(SignalFuture<Integer> value) {
						System.out.println("VNC process stopped: "+value.getSimple());
					}
				});
				s=ss.accept();
				System.out.println("X11vnc connected to local VNC server endpoint as reflect client.");
				UtilProcess.streamErrorOfProcess(s.getInputStream(), back);
				os=s.getOutputStream();
				UtilProcess.streamErrorOfProcess(p.getErrorStream(), System.err);
				UtilProcess.streamErrorOfProcess(p.getInputStream(), System.out);
//				waitUntilServerportisAccessible(port);
			}finally
			{
				ss.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void waitUntilServerportisAccessible(int port) {
		int n=0;
		while(n<30)
		{
			if(processresult.getSimple()!=null)
			{
				return;
			}
			try {
				try(Socket s=new Socket("localhost", port))
				{
					// Return if connection did not fail.
					return;
				}
			} catch (IOException e) {
				System.out.println("Can not yet connect to port: "+port+" retries: "+n);
				// Tolerate can not connect exception until timeout
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			n++;
		}
	}

	@Override
	public void readFully(InputStream is, int len) throws IOException {
		IChannelReader.pipeToFully(is, len, buffer, os);
	}

	public void dispose() {
		for(Reg r: clients)
		{
			r.close();
		}
		clients.clear();
		if(p!=null){
			p.destroy();
		}
	}

	@Override
	public StreamRegistration registerClient(VideoConnection videoConnection, int clientChannel) {
		Reg ret=new Reg();
		try {
			waitUntilServerportisAccessible(port);
			Socket s=new Socket("localhost", port);
			ChannelOutputStream cos=videoConnection.getConnection().getMultiplexer().createStream();
			ret.setChannels(videoConnection, s, clientChannel, cos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clients.add(ret);
		return ret;
	}

	@Override
	public IStreamData getStreamData() {
		return new StreamDataDuplex(channel, back.getChannel());
	}

}
