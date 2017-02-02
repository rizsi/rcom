package com.rizsi.rcom.cli;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.rizsi.rcom.DemuxedConnection;
import com.rizsi.rcom.gui.Gui;
import com.rizsi.rcom.ssh.Connect;

import hu.qgears.commons.UtilFile;

public class Launcher {
	public static void main(String[] args) throws Exception {
		UtilFile.defaultBufferSize.set(DemuxedConnection.bufferSize);
		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);

		if (args.length > 0) {
			String[] subArgs = new String[args.length - 1];
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);
			if ("server".equals(args[0])) {
				new Server().main(subArgs);
				return;
			} else if ("client".equals(args[0])) {
				new Client().main(subArgs);
				return;
			} else if ("connect".equals(args[0])) {
				new Connect().main(subArgs);
				return;
			} else if ("gui".equals(args[0])) {
				Gui.commandline(subArgs);
				return;
			}
		}
		System.out.println(new HelpTemplate().generate());
	}
}
