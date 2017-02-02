package com.rizsi.rcom.platform;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.util.ChainList;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.UtilString;

public class WindowsPlatform {

	public static Map<String, WebCamParameter> getCameras(AbstractRcomArgs args) throws Exception
	{
		Map<String, WebCamParameter> ret = new TreeMap<>();
		ChainList<String> command=new ChainList<>(args.program_ffmpeg, "-list_devices", "true", "-f", "dshow", "-i", "dummy");
		List<String> camIds=parseCamIds(streamError(new ProcessBuilder(command).start()));
		for(String dev: camIds)
		{
			ChainList<String> sizesCommand=new ChainList<String>(args.program_ffmpeg, "-f", "dshow", "-list_options", "true", "-i", "video=\""+dev+"\"");
			String data=streamError(new ProcessBuilder(sizesCommand).start());
			parseCamModes(data, dev, ret);
		}
		return ret;
	}
	
	private static String streamError(Process p) throws InterruptedException, ExecutionException
	{
		byte[] errOut = UtilProcess.saveOutputsOfProcess(p).get().getB();
		return new String(errOut, StandardCharsets.UTF_8);
	}

	public static List<String> parseCamIds(String execute) {
		List<String> ret=new ArrayList<>();
		List<String> devs = UtilString.split(execute, "\r\n");
		boolean segment=false;
		for (String line : devs) {
			if (line.startsWith("[dshow @") && line.contains("] DirectShow video devices")) {
				segment=true;
			}
			if(line.startsWith("[dshow @") && line.contains("] DirectShow audio devices"))
			{
				segment=false;
			}
			if (line.startsWith("[dshow @") && line.contains("]  \"")&&segment) {
				int idx=line.indexOf("]  \"");
				String dev = line.substring(idx+4);
				dev=dev.substring(0, dev.length()-1);
				ret.add(dev);
			}
		}
		return ret;
	}

	public static void parseCamModes(String data, String dev, Map<String, WebCamParameter> ret) {
		List<String> lines = UtilString.split(data, "\r\n");
		for (String l : lines) {
			if (l.startsWith("[dshow @")) {
				int idx=l.lastIndexOf(" s=");
				if(idx>0)
				{
					int idx2=l.indexOf(" ", idx+1);
					if(idx2>0)
					{
						String size=l.substring(idx+3,  idx2);
						idx = size.indexOf("x");
						if (idx > 0) {
							String xs = size.substring(0, idx);
							String ys = size.substring(idx + 1);
							try {
								int x = Integer.parseInt(xs);
								int y = Integer.parseInt(ys);
								WebCamParameter p = new WebCamParameter(x, y, dev);
								// TODO framerate
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
}
