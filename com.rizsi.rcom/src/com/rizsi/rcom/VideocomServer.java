package com.rizsi.rcom;

import java.util.HashMap;
import java.util.Map;

import com.rizsi.rcom.cli.ServerCliArgs;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.IMultiplexer;

public class VideocomServer implements IVideocomServer
{
	private ServerCliArgs args;
	private int nClient;
	private Map<String, Room> rooms=new HashMap<>();
	private Map<String, VideoConnection> users=new HashMap<>();

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
		synchronized (this) {
			String un=""+conn.getUserName()+" - "+userName;
			System.out.println("Connect: "+un);
			if(users.containsKey(un))
			{
				int i=2;
				while(users.containsKey(un+"_"+i))
				{
					i++;
				}
				un=un+"_"+i;
			}
			VideoConnection c=new VideoConnection(this, args, conn, nClient++, un);
			users.put(un, c);
			return c;
		}
	}
	
	public ServerCliArgs getArgs() {
		return args;
	}

	public Room enterRoom(VideoConnection videoConnection, String roomName) {
		synchronized (this) {
			Room ret=rooms.get(roomName);
			if(ret==null)
			{
				ret=new Room(this, roomName);
				rooms.put(roomName, ret);
			}
			ret.addUser(videoConnection);
			return ret;
		}
	}

	public void removeRoom(String name) {
		synchronized (this) {
			rooms.remove(name);
		}
	}

	public void removeUser(VideoConnection videoConnection) {
		synchronized (this) {
			users.remove(videoConnection.getUserName());
		}
	}
}
