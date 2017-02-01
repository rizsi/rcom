package com.rizsi.rcom.cli;

import java.io.File;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.VideoServerTCPListener;

import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

public class ServerCliArgs extends AbstractRcomArgs
{
	@JOHelp("Host to bind the server port to")
	public String host="localhost";
	@JOHelp("Port to bind the server port to")
	public int port=VideoServerTCPListener.port;
	@JOHelp("~/.ssh/authorized_keys file target to generate by the program. (If this, keyDir and connectCommand is present then this file is periodically updated with the authorized users.)")
	public File authFile;
	@JOHelp("Folder where the authorized users .pub identifiers are found. (Similar to gitolite configuration keydir)")
	public File keyDir;
	@JOHelp("Timeout of periodic keydir update loop.")
	public long keyDirUpdateTimeoutMillis=1000;
	@JOHelp("If set then this command is executed before updating the keydir. Example: 'git pull -C keydir'")
	public String beforeKeyDirUpdateCommand;
	@JOHelp("Command to execute to connect ssh clients to the server. User name is appended to this command and it will be the command executed by the ssh server for the connected clients.")
	public String connectCommand="java -jar /home/rcom/video.jar -Xmx4m connect --user";
	@JOHelp("Do not execute the server. Can be used to only run the auth file updater mechanism.")
	@JOSimpleBoolean
	public boolean disableServer;
}
