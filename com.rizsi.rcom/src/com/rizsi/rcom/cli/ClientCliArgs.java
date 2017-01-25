package com.rizsi.rcom.cli;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class ClientCliArgs extends AbstractCliArgs
{
	@JOSimpleBoolean
	@JOHelp("Stream webcam video source when connected to the server.")
	public boolean webcam;
	@JOSimpleBoolean
	@JOHelp("Stream microphone audio source when connected to the server.")
	public boolean audio;
	@JOSimpleBoolean
	@JOHelp("Stream screen as VNC session when connected to the server.")
	public boolean vnc;
	@JOSimpleBoolean
	@JOHelp("Do not use stdin as message source.")
	public boolean disableStdinMessaging;
}
