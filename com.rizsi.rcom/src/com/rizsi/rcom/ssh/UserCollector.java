package com.rizsi.rcom.ssh;

import java.io.File;
import java.io.IOException;

import hu.qgears.commons.UtilFile;

public class UserCollector extends Thread
{
	private File users;
	private File auth;
	private String command;
	public UserCollector(File users, File auth, String command) {
		super("Update authorized_keys");
		this.users=users;
		this.auth=auth;
		this.command=command;
	}
	@Override
	public void run() {
		while(true)
		{
			try {
				String s=new UserCollectorTemplate().generate(users, command);
				String prev=null;
				try {
					UtilFile.loadAsString(auth);
				} catch (Exception e) {
				}
				if(!s.equals(prev))
				{
					auth.getParentFile().mkdirs();
					UtilFile.saveAsFile(auth, s);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
