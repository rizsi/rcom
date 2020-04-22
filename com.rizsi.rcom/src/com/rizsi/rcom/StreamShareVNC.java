package com.rizsi.rcom;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.rizsi.rcom.util.ChainList;
import com.rizsi.rcom.vnc.VncForwardingPorts;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.UtilString;
import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.commons.signal.Slot;
import nio.multiplexer.InputStreamReceiver;
import nio.multiplexer.OutputStreamSender;

public class StreamShareVNC extends StreamShare {
	class Reg implements StreamRegistration
	{
		OutputStreamSender cos;
		InputStreamReceiver isr;
		private int clientChannel;
		private Socket s;
		
		public Reg()
		{
			super();
		}
		public void connect(VideoConnection videoConnection, int clientChannel) throws UnknownHostException, IOException {
			waitUntilServerportisAccessible(ports.getRfbport());
			s=new Socket("localhost", ports.getRfbport());
			this.clientChannel=clientChannel;
			this.cos=new OutputStreamSender(videoConnection.getConnection(), bufferSize, true);
			isr=new InputStreamReceiver(bufferSize, true);
			isr.register(videoConnection.getConnection(), clientChannel);
		}

		@Override
		public void close() {
			try {
				cos.close(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				isr.close(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public IStreamData getData() {
			return new StreamDataDuplex(clientChannel, cos.getId());
		}

		@Override
		public void launch() {
			try {
				ConnectStreams.startStreamThread(s.getInputStream(), cos.os);
				ConnectStreams.startStreamThread(isr.in, s.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static int bufferSize=1000000;
	private IStreamData streamData;
	private List<Reg> clients=new ArrayList<>();
	private OutputStreamSender back;
	private Socket s;
	private VncForwardingPorts ports;
	private SignalFutureWrapper<Integer> processresult;
	private InputStreamReceiver isr;
	private Process p;
	public StreamShareVNC(VideoConnection videoConnection, int channel, StreamParameters params) {
		super(videoConnection, params);
		ports=videoConnection.getServer().getVncPortsManager().allocateVNCPorts();
		back=new OutputStreamSender(videoConnection.getConnection(), bufferSize, true);
		isr=new InputStreamReceiver(bufferSize, true);
		try {
			ServerSocket ss=new ServerSocket();
			try
			{
				ports.bindLocalPort(ss);
				ChainList<String> command=new ChainList<>(videoConnection.getArgs().program_x11vnc).addcall(
						UtilString.split("-reflect localhost:"+ports.getN()+" -forever -rfbport "+ports.allocateRfbport()+" -localhost", " "));
				command.addcs("-rfbportv6", "-1"); // See: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=672449
				command.addcs("-noipv6"); // See: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=672449
				System.out.println("VNC server command: "+UtilString.concat(command, " "));
				p=new ProcessBuilder(command).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
				processresult=UtilProcess.getProcessReturnValueFuture(p);
				processresult.addOnReadyHandler(new Slot<SignalFuture<Integer>>() {
					
					@Override
					public void signal(SignalFuture<Integer> value) {
						System.out.println("VNC process stopped: "+value.getSimple());
					}
				});
				s=ss.accept();
				System.out.println("X11vnc connected to local VNC server endpoint as reflect client.");
				UtilProcess.streamErrorOfProcess(s.getInputStream(), back.os);
			}finally
			{
				ss.close();
			}
			isr.register(videoConnection.getConnection(), channel);
			ConnectStreams.startStreamThread(isr.in, s.getOutputStream());
			streamData=new StreamDataDuplex(channel, back.getId());
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

	public void dispose() {
		for(Reg r: clients)
		{
			r.close();
		}
		clients.clear();
		if(p!=null){
			p.destroy();
		}
		if(isr!=null)
		{
			try {
				isr.close(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isr=null;
		}
		if(s!=null)
		{
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			s=null;
		}
		if(ports!=null)
		{
			conn.getServer().submitTimeout(5000, new Runnable() {
				@Override
				public void run() {
					if(ports!=null){ports.dispose(); ports=null;}
				}
			});
		}
	}

	@Override
	public StreamRegistration registerClient(VideoConnection videoConnection, int clientChannel) {
		Reg ret=new Reg();
		try {
			ret.connect(videoConnection, clientChannel);
			clients.add(ret);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	@Override
	public IStreamData getStreamData() {
		return streamData;
	}

}
