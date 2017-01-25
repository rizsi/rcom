package com.rizsi.rcom;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

public class VideocomServer implements IVideocomServer
{
	Room r=new Room();

	@Override
	public String getVersion() {
		return "Videocom 0.0.0";
	}

	@Override
	public long getNanoTime() {
		return System.nanoTime();
	}

	@Override
	public IVideocomConnection connect(String userName) {
		CoolRMIRemoter remoter=CoolRMIRemoter.getCurrentRemoter();
		DemuxedConnection conn=(DemuxedConnection)remoter.getConnection();
		System.out.println("Connect: "+conn.userName+" "+userName);
		return r.connect(remoter, userName);
	}

}
