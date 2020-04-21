package com.rizsi.rcom;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.rizsi.rcom.util.ChainList;

import hu.qgears.commons.ConnectStreams;
import nio.multiplexer.IMultiplexer;
import nio.multiplexer.InputStreamReceiver;
import nio.multiplexer.OutputStreamSender;

public class StreamSinkVNC extends StreamSink
{

	StreamParametersVNC streamParametersVNC;
	public StreamSinkVNC(StreamParametersVNC streamParametersVNC) {
		this.streamParametersVNC=streamParametersVNC;
	}

	@Override
	public void dispose() {
		if(p!=null)
		{
			p.destroy();
			p=null;
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
	}
	private Process p;
	private Socket s;
	private InputStreamReceiver isr;
	private OutputStreamSender oss;
	@Override
	public void start(AbstractRcomArgs args, IVideocomConnection conn, IMultiplexer multiplexer)
			throws Exception {
		int n=9;
		int port=5900+n;
		try(ServerSocket ss=new ServerSocket())
		{
			ss.bind(new InetSocketAddress("localhost", port));
			ChainList<String> command=new ChainList<>(args.program_vncviewer, "-ViewOnly", "localhost:"+n);
			p=new ProcessBuilder(command).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
			s=ss.accept();
		}
		oss=new OutputStreamSender(multiplexer, StreamShareVNC.bufferSize, true);
		IStreamData strd=conn.registerStream(streamParametersVNC.name, oss.getId());
		if(!(strd instanceof StreamDataDuplex))
		{
			System.err.println("Server does not support VNC - VNC sent by other user is cancelled");
			return;
		}
		StreamDataDuplex stream=(StreamDataDuplex)strd;
		isr=new InputStreamReceiver(StreamShareVNC.bufferSize, true);
		isr.register(multiplexer, stream.backChannel);
		conn.launchStream(streamParametersVNC.name);
		ConnectStreams.startStreamThread(isr.in, s.getOutputStream());
		ConnectStreams.startStreamThread(s.getInputStream(), oss.os);
	}
}
