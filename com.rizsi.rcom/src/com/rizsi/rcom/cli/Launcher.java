package com.rizsi.rcom.cli;

import com.rizsi.rcom.DemuxedConnection;
import com.rizsi.rcom.gui.Gui;
import com.rizsi.rcom.ssh.Connect;

import hu.qgears.commons.UtilFile;

public class Launcher {
	public static void main(String[] args) throws Exception {
		UtilFile.defaultBufferSize.set(DemuxedConnection.bufferSize);
		if(args.length>0)
		{
			String[] subArgs=new String[args.length-1];
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);
			if("server".equals(args[0]))
			{
				new Server().main(subArgs);
				return;
			}else if("client".equals(args[0]))
			{
				new Client().main(subArgs);
				return;
			}else if("connect".equals(args[0]))
			{
				new Connect().main(subArgs);
				return;
			}else if("gui".equals(args[0]))
			{
				Gui.commandline(subArgs);
				return;
			}
		}
		System.out.println(new HelpTemplate().generate());
	}
}
