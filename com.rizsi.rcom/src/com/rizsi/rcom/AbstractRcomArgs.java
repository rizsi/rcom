package com.rizsi.rcom;

import com.rizsi.rcom.platform.EPlatform;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class AbstractRcomArgs {
	@JOHelp("ffmpeg is started using this command (Used by video sharing and showing client). It is possible to set full path if necessary.")
	public String program_ffmpeg="ffmpeg";
	@JOHelp("x11vnc is started using this command (Used by x11 sharing Linux client and th server). It is possible to set full path if necessary.")
	public String program_x11vnc="x11vnc";
	@JOHelp("VNC viewer is started using this command (Used by x11 showing Linux client). It is possible to set full path if necessary.")
	public String program_vncviewer="xtightvncviewer";
	@JOHelp("v4l2-ctl is started using this command (Used by webcam sharing Linux client). It is possible to set full path if necessary.")
	public String program_v4l2="v4l2-ctl";
	@JOHelp("ssh is started using this command (Used by secure connected client). It is possible to set full path if necessary.")
	public String program_ssh="ssh";
	@JOHelp("Disable using pactl to enable echo cancel. If not disabled then executes 'pactl list' to check if echo-cancel is activated and activates it if not: 'pactl load-module module-echo-cancel'")
	public boolean disablePactlEchoCancel;
	@JOHelp("The platform the program is running on. The parameters of the launched executables (ffmpeg webcam grabber, VNC) depend on the platform.")
	public EPlatform platform;
	@JOSimpleBoolean
	@JOHelp("Disable VNC support. VNC is enabled by default because the implementation is now considered safe.")
	public boolean disableVNC;
	@JOHelp("Size of streaming buffers for audio and video.")
	public int bufferSize=VideoConnection.BUFFER_SIZE_DEFAULT;
	public AbstractRcomArgs() {
		if(System.getProperty("os.name").contains("Linux"))
		{
			platform=EPlatform.linux;
		}else
		{
			platform=EPlatform.windows;
		}
	}
	public void apply() {
		VideoConnection.bufferSize=bufferSize;
	}
}
