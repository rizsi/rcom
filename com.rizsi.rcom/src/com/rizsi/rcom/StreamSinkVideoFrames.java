package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rizsi.rcom.gui.IVideoStreamContainer;
import com.rizsi.rcom.util.VideoStreamProcessor;

public class StreamSinkVideoFrames extends StreamSinkSimplex implements IVideoStreamContainer
{
	final public StreamParametersVideo p;
	private OutputStream os;
	private byte[] buffer=new byte[DemuxedConnection.bufferSize];
	private VideoStreamProcessor proc;
	private AbstractRcomArgs args;
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
	}
	@Override
	public void readFully(InputStream is, int len) throws IOException {
		IChannelReader.pipeToFully(is, len, buffer, os);
	}
	@Override
	public void dispose() {
		if(proc!=null)
		{
			proc.close();
			proc=null;
		}
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

}
