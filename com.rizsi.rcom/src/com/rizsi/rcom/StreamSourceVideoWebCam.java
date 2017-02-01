package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rizsi.rcom.cli.Client;
import com.rizsi.rcom.gui.IVideoStreamContainer;
import com.rizsi.rcom.util.TeeOutputStream;
import com.rizsi.rcom.util.VideoStreamProcessor;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.UtilProcess;
import nio.multiplexer.OutputStreamSender;

public class StreamSourceVideoWebCam implements AutoCloseable, IVideoStreamContainer
{
	private WebCamParameter wcp;
	private Process p;
	private OutputStreamSender oss;
	private StreamParametersVideo params;
	private IVideocomConnection conn;
	private VideoStreamProcessor stream;
	
	public StreamSourceVideoWebCam(WebCamParameter wcp) {
		super();
		this.wcp = wcp;
	}

	public void start(Client c, String streamName) throws IOException {
		conn=c.conn;
		oss=new OutputStreamSender(c.getMultiplexer(), VideoConnection.bufferSize);
		c.conn.shareStream(oss.getId(), params=new StreamParametersVideo(streamName, c.id, wcp.getW(), wcp.getH(), "mpegts"));
		stream=new VideoStreamProcessor(c.getArgs(), params.width, params.height, params.width/2, params.height/2, params.encoding);
		String command=c.getArgs().platform.createWebCamStreamCommand(c.getArgs(), wcp, params);
		System.out.println("Webcam command: $ "+command);
		p=Runtime.getRuntime().exec(command);
		InputStream is=p.getInputStream();
		InputStream err=p.getErrorStream();
//		UtilProcess.streamErrorOfProcess(err, System.err);
		UtilProcess.streamErrorOfProcess(err, new NullOutputStream());
		UtilProcess.streamErrorOfProcess(is, new TeeOutputStream(new OutputStream[]{oss.os, stream.launch()}));
	}

	@Override
	public void close()
	{
		if(params!=null)
		{
			conn.unshare(params);
			params=null;
		}
		if(p!=null)
		{
			p.destroy();
			p=null;
		}
		if(oss!=null)
		{
			try {
				oss.close(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			oss=null;
		}
		if(stream!=null)
		{
			stream.close();
		}
	}
	public VideoStreamProcessor getVideoStream() {
		return stream;
	}
	private Object guiObject;
	@Override
	public void setGuiObject(Object o) {
		this.guiObject=o;
	}
	@Override
	public Object getGuiObject() {
		return guiObject;
	}
}
