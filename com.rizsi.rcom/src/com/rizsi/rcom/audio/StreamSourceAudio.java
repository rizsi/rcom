package com.rizsi.rcom.audio;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import javax.sound.sampled.AudioFormat;

import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.cli.Client;

import hu.qgears.commons.UtilProcess;
import nio.multiplexer.OutputStreamSender;

public class StreamSourceAudio implements AutoCloseable {
	public static AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	private IVideocomConnection conn;
	private StreamParametersAudio params;
	public static final int requestBufferSize=512;
	private volatile ICapture capture;
	private OutputStreamSender oss;
	
	private boolean isPulseAudioEchoCancelEnabled() throws IOException
	{
		ProcessBuilder pb=new ProcessBuilder("pactl", "list");
		pb.redirectError(Redirect.INHERIT);
		Process p=pb.start();
		String res=UtilProcess.execute(p);
		return res.indexOf("module-echo-cancel")>-1;
	}
	private void enablePulseAudioEchoCancel() throws IOException
	{
		System.out.println("pactl load-module module-echo-cancel");
		ProcessBuilder pb=new ProcessBuilder("pactl", "load-module", "module-echo-cancel");
		pb.redirectError(Redirect.INHERIT);
		pb.redirectOutput(Redirect.INHERIT);
		Process p=pb.start();
		UtilProcess.execute(p);
	}

	public void start(Client client, String streamName) {
		if(!client.getArgs().disablePactlEchoCancel)
		{
			try {
				if(isPulseAudioEchoCancelEnabled())
				{
					System.out.println("PulseAudio Echo cancel is already enabled.");
				}else
				{
					enablePulseAudioEchoCancel();
					if(isPulseAudioEchoCancelEnabled())
					{
						System.out.println("PulseAudio Echo cancel enabled now.");
					}else
					{
						System.err.println("Could not turn on PulseAudio echo cancel");
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		conn=client.conn;
		oss=new OutputStreamSender(client.getMultiplexer(), VideoConnection.bufferSize, false);
		client.conn.shareStream(oss.getId(), params=new StreamParametersAudio(streamName, client.id));
		capture=client.getAudio().startCapture(oss.os);
	}

	@Override
	public void close() {
		if(capture!=null)
		{
			capture.close();
			capture=null;
		}
		if(params!=null)
		{
			conn.unshare(params);
			params=null;
		}
		oss.close(null);
	}

}
