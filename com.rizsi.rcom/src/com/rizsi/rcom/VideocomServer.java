package com.rizsi.rcom;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.IMultiplexer;

public class VideocomServer implements IVideocomServer
{
	Room r=new Room(this);
	private ServerCliArgs args;
	
	public VideocomServer(ServerCliArgs args) {
		this.args=args;
	}

	@Override
	public String getVersion() {
		return VideoServerTCPListener.serverID;
	}

	@Override
	public long getNanoTime() {
		return System.nanoTime();
	}

	@Override
	public IVideocomConnection connect(String userName) {
		GenericCoolRMIRemoter remoter=CoolRMIRemoter.getCurrentRemoter();
		IMultiplexer conn=((CoolRMINioRemoter)remoter).getNioMultiplexer();
		System.out.println("Connect: "+conn.getUserName()+" "+userName);
		return r.connect(remoter, conn.getUserName()+"-"+userName);
	}
	
	public ServerCliArgs getArgs() {
		return args;
	}

}
