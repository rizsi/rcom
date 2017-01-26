package com.rizsi.rcom.webcam;

import java.io.FileOutputStream;

import com.rizsi.rcom.StreamParametersVideo;
import com.rizsi.rcom.cli.ClientCliArgs;

import hu.qgears.commons.ConnectStreams;

public class StreamWebCam {
	public static void main(String[] args) throws Exception {
		ClientCliArgs a=new ClientCliArgs();
		WebCamParameter p=a.platform.getCameras(a).values().iterator().next();
		String encoding="mpegts";
		StreamParametersVideo params=new StreamParametersVideo("name", 1, p.getW(), p.getH(), encoding);
		String wc=a.program_ffmpeg+" -f v4l2 -video_size "+p.getW()+"x"+p.getH()+" -i /dev/video0 -f rawvideo -pix_fmt rgb24 -";
		Process capture=Runtime.getRuntime().exec(wc);
		String enc=a.program_ffmpeg+" -f rawvideo -pix_fmt rgb24 -s:v "+p.getW()+"x"+p.getH()+" -i - -f "+encoding+" -";
		Process encode=Runtime.getRuntime().exec(enc);
		ConnectStreams.startStreamThread(capture.getInputStream(), encode.getOutputStream());
		ConnectStreams.startStreamThread(encode.getInputStream(), new FileOutputStream("/tmp/out.mpeg"));
		ConnectStreams.startStreamThread(capture.getErrorStream(), System.err);
		ConnectStreams.startStreamThread(encode.getErrorStream(), System.err);
//		$ ffmpeg -f v4l2 -video_size 160x120 -i /dev/video0 -f rawvideo -pix_fmt rgb24 - |ffmpeg -f rawvideo -pix_fmt rgb24 -s:v 160x120 -i - -f mpeg out.mpeg
	}
}
