package com.rizsi.rcom;

import java.io.IOException;
import java.io.OutputStream;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilProcess;

/**
 * Open video player window as a separate process to ply this stream.
 */
public class StreamSinkVideo extends StreamSinkSimplex
{
	StreamParametersVideo p;
	private OutputStream os;
	private Process player;
	public StreamSinkVideo(StreamParametersVideo p) {
		super(p.name);
		this.p=p;
	}
	@Override
	public void start() throws IOException
	{
		player=Runtime.getRuntime().exec("ffplay -f "+p.encoding+" -analyzeduration 0 -fpsprobesize 32000 -probesize 32000 -sync ext -");
		os=player.getOutputStream();
		UtilProcess.streamErrorOfProcess(player.getInputStream(), new NullOutputStream());
		UtilProcess.streamErrorOfProcess(player.getErrorStream(), new NullOutputStream());
		ConnectStreams.startStreamThread(receiver.in, os);
	}
	@Override
	public void dispose() {
		if(player!=null)
		{
			player.destroy();
			player=null;
		}
		super.dispose();
	}

}
