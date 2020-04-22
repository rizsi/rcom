package com.rizsi.rcom;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.rizsi.rcom.cli.ServerCliArgs;
import com.rizsi.rcom.vnc.VncPortsManager;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.IMultiplexer;

public class VideocomServer implements IVideocomServer
{
	private static final Logger LOG = Logger.getLogger(VideocomServer.class);
	private ServerCliArgs args;
	private int nClient;
	private Map<String, Room> rooms=new HashMap<>();
	private Map<String, VideoConnection> users=new HashMap<>();
	private Timer timer;
	private VncPortsManager vncPortsManager;

	public VideocomServer(ServerCliArgs args) {
		this.args=args;
		timer=new Timer(true);
		if(!args.disableVNC)
		{
			vncPortsManager=new VncPortsManager(args);
		}
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

	public void submitTimeout(long millis, final Runnable runnable) {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					LOG.error(e);
				}
			}
		}, millis);
	}

	public VncPortsManager getVncPortsManager() {
		return vncPortsManager;
	}
}
