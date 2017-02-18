package com.rizsi.rcom.cli;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line.Info;

public class ListAudio {

	public static void commandline(String[] subArgs) {
		final Mixer mixer = AudioSystem.getMixer(null);
		System.out.println("Listing audio channels...");
		for(Info info: mixer.getTargetLineInfo())
		{
			DataLine.Info di=(DataLine.Info) info;
			System.out.println("Line: "+di);
			for(AudioFormat f: di.getFormats())
			{
				System.out.println("Supported format: "+f);
			}
		}
		System.out.println("Listing audio channels DONE");
	}

}
