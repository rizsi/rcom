package com.rizsi.rcom;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

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
		DemuxedConnection conn=(DemuxedConnection)((CoolRMIRemoter)remoter).getConnection();
		System.out.println("Connect: "+conn.userName+" "+userName);
		return r.connect(remoter, userName);
	}
	
	public ServerCliArgs getArgs() {
		return args;
	}

}
