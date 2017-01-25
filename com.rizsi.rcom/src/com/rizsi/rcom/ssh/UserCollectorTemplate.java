package com.rizsi.rcom.ssh;

import java.io.File;
import java.io.IOException;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.rtemplate.runtime.DummyCodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class UserCollectorTemplate extends RAbstractTemplatePart {
	public UserCollectorTemplate() {
		super(new DummyCodeGeneratorContext());
	}
	public String generate(File folder, String command) throws IOException
	{
		write("# RCom start\n");
		for(File f: UtilFile.listFiles(folder))
		{
			try {
				String fileName=f.getName();
				if(fileName.endsWith(".pub"))
				{
					String userName=fileName.substring(0, fileName.length()-4);
					if(validateUserName(userName))
					{
						String key=UtilFile.loadAsString(f);
						String line=UtilString.split(key, "\r\n").get(0).trim();
						write("command=\"");
						writeObject(command);
						write(" ");
						writeObject(userName);
						write("\",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty ");
						writeObject(line);
						write("\n");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		write("# RCom end\n");
		finishDeferredParts();
		return getTemplateState().getOut().toString();
	}
	private boolean validateUserName(String userName) {
		for(char ch:userName.toCharArray())
		{
			if(ch>126)
			{
				// lower ASCII only and DEL is prohibited
				return false;
			}
			if(ch<='@')
			{
				// characters below are possible escaping characters also disallowed
				return false;
			}
		}
		return true;
	}
}
