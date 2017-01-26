package com.rizsi.rcom.platform;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.StreamParametersVideo;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.UtilString;

public enum EPlatform {
	linux{

		@Override
		public Map<String, WebCamParameter> getCameras(AbstractRcomArgs args) throws Exception {
			Map<String, WebCamParameter> ret = new TreeMap<>();
			List<String> devs = UtilString.split(UtilProcess.execute(args.program_v4l2+" --list-devices"), "\r\n");
			for (String line : devs) {
				if (line.startsWith("\t")) {
					String dev = line.trim();
					System.out.println("Dev: '" + dev + "'");
					String sizesCmd = args.program_ffmpeg+" -f v4l2 -list_formats all -i " + dev;
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

		@Override
		public String createWebCamStreamCommand(AbstractRcomArgs args, WebCamParameter wcp,
				StreamParametersVideo params) {
			return args.program_ffmpeg+" -f v4l2 -framerate "+params.framerate+" -video_size "+params.width+"x"+params.height+" -i "+wcp.getDevice()+" -f "+params.encoding+" -";
		}
	},
	windows{

		@Override
		public Map<String, WebCamParameter> getCameras(AbstractRcomArgs args) throws Exception {
			return WindowsPlatform.getCameras(args);
		}
		
		@Override
		public String createWebCamStreamCommand(AbstractRcomArgs args, WebCamParameter wcp,
				StreamParametersVideo params) {
			// TODO framerate
			String framerate=" -framerate "+params.framerate+" ";
			return args.program_ffmpeg+" -f dshow -video_size "+params.width+"x"+params.height+" -i video=\""+wcp.getDevice()+"\" -f "+params.encoding+" -";
		}
		
	};

	abstract public Map<String, WebCamParameter> getCameras(AbstractRcomArgs args) throws Exception;

	abstract public String createWebCamStreamCommand(AbstractRcomArgs abstractRcomArgs, WebCamParameter wcp, StreamParametersVideo params);
}
