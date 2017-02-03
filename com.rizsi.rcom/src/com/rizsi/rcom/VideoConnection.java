package com.rizsi.rcom;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.coolrmi.messages.CoolRMICall;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.IMultiplexer;

public class VideoConnection implements IVideocomConnection
{
	public static final String serviceID="RCOM0.0.3";
	public static final String clientID=serviceID+"client";
	public static final String serverID=serviceID+"server";
	public static final byte[] clientIDBS=clientID.getBytes(StandardCharsets.UTF_8);
	public static final byte[] serverIDBS=serverID.getBytes(StandardCharsets.UTF_8);
	public static final int BUFFER_SIZE_DEFAULT = 1048576;
	public static int bufferSize=BUFFER_SIZE_DEFAULT;
	private IMultiplexer c;
	private Room room;
	private int id;
	private IVideocomCallback callback;
	private String userName;
	private Map<String, StreamRegistration> registrations=new HashMap<>();
	private VideocomServer server;
	public VideoConnection(VideocomServer server, AbstractRcomArgs args, IMultiplexer c, int id, String userName) {
		this.c=c;
		this.id=id;
		this.userName=userName;
		this.server=server;
		CoolRMINioRemoter remoter=(CoolRMINioRemoter)CoolRMIRemoter.getCurrentRemoter();
		remoter.closedEvent.addListener(new UtilEventListener<CoolRMINioRemoter>() {
			@Override
			public void eventHappened(CoolRMINioRemoter msg) {
				dispose();
			}
		});
	}
	public void init()
	{
		c.getClosedEvent().addListener(new UtilEventListener<Exception>() {
			@Override
			public void eventHappened(Exception msg) {
				dispose();
			}
		});
	}

	@Override
	public IStreamData shareStream(int channel, StreamParameters params) {
		if(params.sourceClient!=id)
		{
			throw new IllegalArgumentException();
		}
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		StreamShare share=params.createShare(this, channel);
		room.addShare(share);
		return share.getStreamData();
	}

	@Override
	public void sendMessage(String message) {
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		room.messageReceived(this, userName+": "+message);
	}

	@Override
	public void registerCallback(IVideocomCallback callback) {
		this.callback=callback;
		CoolRMICall.getCurrentCall().asyncCall(null);
		// room is not entered by the time of this call.
	}

	public void callbackMessage(String message) {
		if(callback!=null)
		{
			CoolRMICall.getCurrentCall().asyncCall(null);
			callback.message(message);
		}
	}

	public void callbackCurrentShares(List<StreamParameters> values) {
		if(callback!=null)
		{
			CoolRMICall.getCurrentCall().asyncCall(null);
			callback.currentShares(values);
		}		
	}
	public void dispose()
	{
		if(room!=null)
		{
			room.removeUser(this);
		}
		server.removeUser(this);
	}
	@Override
	public int getId() {
		return id;
	}
	@Override
	public IStreamData registerStream(String name, int channel) {
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		StreamShare s=room.getShare(name);
		if(s==null)
		{
			throw new IllegalArgumentException();
		}
		StreamRegistration reg=s.registerClient(this, channel);
		synchronized (this) {
			registrations.put(name, reg);
			return reg.getData();
		}
	}
	@Override
	public void launchStream(String name) {
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		StreamShare s=room.getShare(name);
		if(s==null)
		{
			throw new IllegalArgumentException();
		}
		synchronized (this) {
			StreamRegistration reg=registrations.get(name);
			if(reg!=null)
			{
				reg.launch();
			}
		}
	}
	@Override
	public void unregisterStream(String name) {
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		StreamShare s=room.getShare(name);
		if(s==null)
		{
			return;
		}
		synchronized (this) {
			StreamRegistration cos=registrations.remove(name);
			if(cos!=null)
			{
				cos.close();
			}
		}
	}
	public IMultiplexer getConnection() {
		return c;
	}
	@Override
	public void unshare(StreamParameters params) {
		if(room==null)
		{
			throw new IllegalStateException("Room not entered");
		}
		StreamShare s=room.getShare(params.name);
		if(s==null||s.conn!=this)
		{
			throw new IllegalArgumentException();
		}
		room.removeShare(s);
		room.updateSharesToClients();
	}
	public AbstractRcomArgs getArgs() {
		return server.getArgs();
	}
	public String getUserName() {
		return userName;
	}
	@Override
	public String enterRoom(String roomName) {
		if(callback==null)
		{
			throw new IllegalStateException("First a callback has to be registered.");
		}
		if(room!=null)
		{
			room.removeUser(this);
			room=null;
		}
		room=server.enterRoom(this, roomName);
		return roomName;
	}
	@Override
	public void leaveRoom() {
		if(room!=null)
		{
			room.removeUser(this);
			room=null;
		}
	}
	public void callbackCurrentUsers(List<String> users) {
		if(callback!=null)
		{
			CoolRMICall.getCurrentCall().asyncCall(null);
			callback.currentUsers(users);
		}		
	}
}
