package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class MixingOutputSimple extends MixingOutput
{
	private SourceDataLine line;
	private Mixer mixer;
	private volatile PipedOutputStream speexCopy;
	
	public MixingOutputSimple(Mixer mixer) {
		super();
		this.mixer = mixer;
	}
	protected void writeAudioOutput(byte[] buffer) throws IOException {
		line.write(buffer, 0, buffer.length);
		if(speexCopy!=null)
		{
			speexCopy.write(buffer);
		}
	}
	public void setSpeexCopy(PipedOutputStream playSink) {
		this.speexCopy=playSink;
	}
	protected void closeAudioOutput() {
		if(line!=null)
		{
			line.close();
			line=null;
		}
	}
	protected void startAudioOutput()
	{
		line.start();
	}
	protected int openAudioOutput() throws LineUnavailableException {
		AudioFormat format = StreamSourceAudio.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		line = (SourceDataLine) mixer.getLine(info);
		try {
			line.open(format, StreamSourceAudio.requestBufferSize);
			return line.getBufferSize();
		} catch(Exception e) {
			closeAudioOutput();
			throw new RuntimeException(e);
		}
	}

}
