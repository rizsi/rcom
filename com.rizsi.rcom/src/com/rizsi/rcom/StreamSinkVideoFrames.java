package com.rizsi.rcom;

import java.io.IOException;
import java.io.OutputStream;

import com.rizsi.rcom.gui.IVideoStreamContainer;
import com.rizsi.rcom.util.VideoStreamProcessor;

import hu.qgears.commons.ConnectStreams;

public class StreamSinkVideoFrames extends StreamSinkSimplex implements IVideoStreamContainer
{
	final public StreamParametersVideo p;
	private OutputStream os;
	private VideoStreamProcessor proc;
	private AbstractRcomArgs args;
	private volatile boolean closed=false;
	public StreamSinkVideoFrames(AbstractRcomArgs args, StreamParametersVideo p) {
		super(p.name);
		this.p=p;
		this.args=args;
	}
	@Override
	public void start() throws IOException
	{
		proc=new VideoStreamProcessor(args, p.width, p.height, p.width, p.height, p.encoding);
		os=proc.launch();
		ConnectStreams.startStreamThread(receiver.in, os);
	}
	@Override
	public void dispose() {
		if(proc!=null)
		{
			proc.close();
			proc=null;
		}
		closed=true;
	}
	@Override
	public VideoStreamProcessor getVideoStream() {
		return proc;
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
	@Override
	public boolean isClosed() {
		return closed;
	}
}
