package com.rizsi.rcom.cli;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.VideoServerTCPListener;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class AbstractCliArgs extends AbstractRcomArgs
{
	@JOHelp("SSH connection string to connect to server. Disables raw TCP connection and overrides host and port if present.")
	public String ssh;
	@JOHelp("Raw TCP connect to this server.")
	public String host="localhost";
	@JOHelp("Raw TCP connect to this server.")
	public int port=VideoServerTCPListener.port;
	@JOSimpleBoolean
	@JOHelp("By default the program sets: PULSE_PROP=\"filter.want=echo-cancel\" it can be disabled using this flag.")
	public boolean disablePulseEchoCancellation;
}
