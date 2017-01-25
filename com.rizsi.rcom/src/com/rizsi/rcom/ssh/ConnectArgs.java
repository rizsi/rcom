package com.rizsi.rcom.ssh;

import com.rizsi.rcom.VideoServerTCPListener;

import joptsimple.annot.JOHelp;

public class ConnectArgs {
	@JOHelp("Connect to this RCOM server.")
	public int port=VideoServerTCPListener.port;
	@JOHelp("This is the authenticated user who is now connected to the server.")
	public String user;
	@JOHelp("Connect to this RCOM server.")
	public String host="localhost";
}
