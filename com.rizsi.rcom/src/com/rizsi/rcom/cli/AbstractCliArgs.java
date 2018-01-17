package com.rizsi.rcom.cli;

import com.rizsi.rcom.AbstractRcomArgs;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class AbstractCliArgs extends AbstractRcomArgs
{
	@JOHelp("Connection string to the server. If it contains a '@' character then it is an ssh user@server address. Otherwise it has to contain a ':' and must be host:port format TCP address.")
	public String connectionString;
	@JOHelp("Room to connect to. If not set the client does not connect automatically to a room.")
	public String room;
	@JOHelp("The requested username. A timestamp will replace it if none is given.")
	public String userName;
	@JOHelp("Disable audio jitter buffer. Not recommended, development feature only.")
	@JOSimpleBoolean
	public boolean disableAudioJitterResampler;
	@JOHelp("Enable echo canceller (Speex). Use this when the operating system does not implement one.")
	@JOSimpleBoolean
	public boolean echoCanceller;
	@JOHelp("speexcmd program path. This program is part of the RCOM project. Must be matching version.")
	public String program_speexcmd="speexcmd";
}
