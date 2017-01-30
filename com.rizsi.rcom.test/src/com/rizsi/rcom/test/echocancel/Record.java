package com.rizsi.rcom.test.echocancel;

import java.io.FileOutputStream;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class Record {
	public static void main(String[] args) throws Exception {
		AudioFormat format=ManualTestEchoCancel.getFormat();
		final Mixer mixer = AudioSystem.getMixer(null);
		Mic m=new Mic(mixer, format, ManualTestEchoCancel.frameSamples);
		m.start();
		try(Scanner br=new Scanner(System.in))
		{
			System.out.println("Press ENTER to start recording");
			br.nextLine();
			try(FileOutputStream fos=new FileOutputStream("/tmp/out.sw"))
			{
				m.setRecord(fos);
				System.out.println("Press ENTER to stop recording");
				br.nextLine();
				m.setRecord(null);
			}
		}
		System.exit(0);
	}
}
