package com.rizsi.rcom.test.echocancel;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import hu.qgears.commons.UtilFile;

public class Replay2 {
	public static void main(String[] args) throws IOException, LineUnavailableException {
		try(Scanner br=new Scanner(System.in))
		{
			String s=br.nextLine();
			byte[] data=UtilFile.loadFile(new File("/tmp/"+s+".sw"));
			System.out.println("Playing: "+s);
			AudioFormat format=ManualTestEchoCancel.getFormat();
			final Mixer mixer = AudioSystem.getMixer(null);
			Play p=new Play(mixer, format, ManualTestEchoCancel.frameSamples)
			{
			};
			p.start();
			p.setSample(data);
		}
	}
}
