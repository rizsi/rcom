package com.rizsi.rcom.test;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.rizsi.rcom.webcam.FFmpegOutputParser;

import hu.qgears.commons.UtilFile;

public class TestFFmpegOutputParser {
	@Test
	public void testGetStreamStartTimestamp() throws IOException
	{
		String output=UtilFile.loadAsString(getClass().getResource("ffmpeg-decode-output.txt"));
		double v=new FFmpegOutputParser().process(new StringReader(output));
		Assert.assertEquals(1.7, v, 0.0001);
	}
}
