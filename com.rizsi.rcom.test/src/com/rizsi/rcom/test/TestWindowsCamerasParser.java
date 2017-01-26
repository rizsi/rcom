package com.rizsi.rcom.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import com.rizsi.rcom.platform.WindowsPlatform;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.UtilFile;

public class TestWindowsCamerasParser {
	@Test
	public void testCamerasParser() throws IOException
	{
		List<String> ids=WindowsPlatform.parseCamIds(UtilFile.loadAsString(getClass().getResource("win-camlist.txt")));
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals("VirtualBox Webcam - USB 2.0 UVC HD Webcam", ids.get(0));
		Map<String, WebCamParameter> params=new TreeMap<>();
		WindowsPlatform.parseCamModes(UtilFile.loadAsString(getClass().getResource("win-cammodes.txt")), "DUMMY", params);
		Assert.assertTrue(params.containsKey("DUMMY 1280x720"));
		Assert.assertTrue(params.containsKey("DUMMY 320x240"));
	}
}
