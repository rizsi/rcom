package com.rizsi.rcom.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.rizsi.rcom.webcam.FFmpegCaptureParser;

import hu.qgears.commons.UtilFile;

public class TestFFmpegWebCamOutputParser {
	@Test
	public void testParse() throws IOException
	{
		List<Integer> frames=new ArrayList<Integer>();
		List<Integer> fpss=new ArrayList<Integer>();
		String s=UtilFile.loadAsString(getClass().getResource("ffmpeg-webcam-output.txt"));
		new FFmpegCaptureParser()
		{

			@Override
			protected void frameReceived(int parseInt) {
				frames.add(parseInt);
			}

			@Override
			protected void setFps(int parseInt) {
				fpss.add(parseInt);
				Assert.assertTrue(frames.isEmpty());
			}
			
		}
		.process(new StringReader(s));
		Assert.assertArrayEquals(new Object[]{30}, fpss.toArray());
		Assert.assertArrayEquals(new Object[]{16,30,45,60,75,90,105,120,135,143}, frames.toArray());
	}
}
