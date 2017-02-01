package com.rizsi.rcom.ssh;

import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.VideoServerTCPListener;

import joptsimple.annot.JOHelp;

public class ConnectArgs {
	@JOHelp("Connect to this RCOM server.")
	public int port=VideoServerTCPListener.port;
	@JOHelp("This is the authenticated user who is now connected to the server.")
	public String user;
	@JOHelp("Connect to this RCOM server.")
	public String host="localhost";
	@JOHelp("Size of streaming buffers")
	public int bufferSize=VideoConnection.BUFFER_SIZE_DEFAULT;
	public void apply() {
		VideoConnection.bufferSize=bufferSize;
	}
}
