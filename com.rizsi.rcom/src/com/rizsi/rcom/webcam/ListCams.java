package com.rizsi.rcom.webcam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.UtilString;

public class ListCams {
	public WebCamParameter queryUser() throws InterruptedException, ExecutionException, IOException
	{
		Map<String, WebCamParameter> ret=getCameras();
		System.out.println("" + ret);
		String[] choices = ret.keySet().toArray(new String[] {});
		String input = (String) JOptionPane.showInputDialog(null, "Select...", "WebCam input mode",
				JOptionPane.QUESTION_MESSAGE, null, // Use
													// default
													// icon
				choices, // Array of choices
				choices[1]); // Initial choice
		if(input==null)
		{
			return null;
		}
		return ret.get(input);
	}
	public Map<String, WebCamParameter> getCameras() throws InterruptedException, ExecutionException, IOException
	{
		Map<String, WebCamParameter> ret = new TreeMap<>();
		List<String> devs = UtilString.split(UtilProcess.execute("v4l2-ctl --list-devices"), "\r\n");
		for (String line : devs) {
			if (line.startsWith("\t")) {
				String dev = line.trim();
				System.out.println("Dev: '" + dev + "'");
				String sizesCmd = "ffmpeg -f v4l2 -list_formats all -i " + dev;
				byte[] errOut = UtilProcess.saveOutputsOfProcess(Runtime.getRuntime().exec(sizesCmd)).get().getB();
				List<String> lines = UtilString.split(new String(errOut, StandardCharsets.UTF_8), "\r\n");
				for (String l : lines) {
					if (l.startsWith("[")) {
						int idx = l.lastIndexOf(':');
						for (String piece : UtilString.split(l.substring(idx), " ")) {
							idx = piece.indexOf("x");
							if (idx > 0) {
								String xs = piece.substring(0, idx);
								String ys = piece.substring(idx + 1);
								try {
									int x = Integer.parseInt(xs);
									int y = Integer.parseInt(ys);
									WebCamParameter p = new WebCamParameter(x, y, dev);
									ret.put(p.toString(), p);
								} catch (Exception e) {
									// Ignore problematic lines
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}
}
