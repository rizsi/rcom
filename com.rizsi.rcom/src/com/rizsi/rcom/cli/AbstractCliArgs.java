package com.rizsi.rcom.cli;

import com.rizsi.rcom.AbstractRcomArgs;

import joptsimple.annot.JOHelp;

public class AbstractCliArgs extends AbstractRcomArgs
{
	@JOHelp("Connection string to the server. If it contains a '@' character then it is an ssh user@server address. Otherwise it has to contain a ':' and must be host:port format TCP address.")
	public String connectionString;
	@JOHelp("Room to connect to. If not set the client does not connect automatically to a room.")
	public String room;
	@JOHelp("The requested username. A timestamp will replace it if none is given.")
	public String userName;
}
