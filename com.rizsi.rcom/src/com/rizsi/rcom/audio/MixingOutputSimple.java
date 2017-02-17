package com.rizsi.rcom.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class MixingOutputSimple extends MixingOutput
{
	private SourceDataLine line;
	protected void writeAudioOutput(byte[] buffer) {
		line.write(buffer, 0, buffer.length);
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
		line = (SourceDataLine) AudioSystem.getLine(info);
		try {
			line.open(format, StreamSourceAudio.requestBufferSize);
			return line.getBufferSize();
		} catch(Exception e) {
			closeAudioOutput();
			throw new RuntimeException(e);
		}
	}

}
