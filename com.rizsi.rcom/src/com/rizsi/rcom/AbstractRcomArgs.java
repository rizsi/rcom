package com.rizsi.rcom;

import com.rizsi.rcom.platform.EPlatform;

import joptsimple.annot.JOHelp;

public class AbstractRcomArgs {
	@JOHelp("ffmpeg is started using this command (Used by video sharing and showing client). It is possible to set full path if necessary.")
	public String program_ffmpeg="ffmpeg";
	@JOHelp("x11vnc is started using this command (Used by x11 sharing Linux client and th server). It is possible to set full path if necessary.")
	public String program_x11vnc="x11vnc";
	@JOHelp("VNC viewer is started using this command (Used by x11 showing Linux client). It is possible to set full path if necessary.")
	public String program_vncviewer="xvnc4viewer";
	@JOHelp("v4l2-ctl is started using this command (Used by webcam sharing Linux client). It is possible to set full path if necessary.")
	public String program_v4l2="v4l2-ctl";
	@JOHelp("ssh is started using this command (Used by secure connected client). It is possible to set full path if necessary.")
	public String program_ssh="ssh";
	@JOHelp("The platform the program is running on. The parameters of the launched executables (ffmpeg webcam grabber, VNC) depend on the platform.")
	public EPlatform platform;
	public AbstractRcomArgs() {
		if(System.getProperty("os.name").contains("Linux"))
		{
			platform=EPlatform.linux;
		}else
		{
			platform=EPlatform.windows;
		}
	}
}