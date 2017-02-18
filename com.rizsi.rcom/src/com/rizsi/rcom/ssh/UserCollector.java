package com.rizsi.rcom.ssh;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilProcess;
import nio.multiplexer.AbstractMultiplexer;

public class UserCollector extends Thread
{
	private ServerCliArgs args;
	public UserCollector(ServerCliArgs args) {
		super("Update authorized_keys");
		this.args=args;
	}
	@Override
	public void run() {
		while(true)
		{
			try {
				if(args.beforeKeyDirUpdateCommand!=null)
				{
					try {
						UtilProcess.getProcessReturnValueFuture(Runtime.getRuntime().exec(args.beforeKeyDirUpdateCommand)).get();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				List<UserKey> keys=new ArrayList<UserKey>();
				for(File f: UtilFile.listFiles(args.keyDir))
				{
					try {
						String fileName=f.getName();
						if(fileName.endsWith(".pub"))
						{
							String userName=fileName.substring(0, fileName.length()-4);
							if(validateUserName(userName))
							{
								UserKey key=new UserKey(userName, UtilFile.loadAsString(f));
								keys.add(key);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				String s=new UserCollectorTemplate().generate(keys, args.host, args.port);
				String prev=null;
				try {
					UtilFile.loadAsString(args.authFile);
				} catch (Exception e) {
				}
				if(!s.equals(prev))
				{
					args.authFile.getParentFile().mkdirs();
					UtilFile.saveAsFile(args.authFile, s);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(args.keyDirUpdateTimeoutMillis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private boolean validateUserName(String userName) {
		
		for(char ch:userName.toCharArray())
		{
			if(ch=='-'||ch=='_')
			{
				
			}else
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
		}
		byte[] bytes=userName.getBytes(StandardCharsets.UTF_8);
		if(bytes.length>AbstractMultiplexer.userNameLength)
		{
			return false;
		}
		return true;
	}
}
