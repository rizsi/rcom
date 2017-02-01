package com.rizsi.rcom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.coolrmi.messages.CoolRMICall;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.ChannelProcessorMultiplexer;

public class VideoConnection implements IVideocomConnection
{
	public static final int BUFFER_SIZE_DEFAULT = 1048576;
	public static int bufferSize=BUFFER_SIZE_DEFAULT;
	private ChannelProcessorMultiplexer c;
	private Room room;
	private int id;
	private IVideocomCallback callback;
	private String userName;
	private Map<String, StreamRegistration> registrations=new HashMap<>();
	private AbstractRcomArgs args;
	public VideoConnection(AbstractRcomArgs args, Room room, GenericCoolRMIRemoter remoter, int id, String userName) {
		this.c=((CoolRMINioRemoter)remoter).getNioMultiplexer();
		this.room=room;
		this.id=id;
		this.userName=userName;
	}
	public void init()
	{
		c.closedEvent.addListener(new UtilEventListener<Exception>() {
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
		StreamShare share=params.createShare(this, channel);
		room.addShare(share);
		return share.getStreamData();
	}

	@Override
	public void sendMessage(String message) {
		room.messageReceived(this, userName+": "+message);
	}

	@Override
	public void registerCallback(IVideocomCallback callback) {
		this.callback=callback;
		CoolRMICall.getCurrentCall().asyncCall(null);
		callback.currentShares(room.getSharesList());
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
		room.remove(this);
	}
	@Override
	public int getId() {
		return id;
	}
	@Override
	public IStreamData registerStream(String name, int channel) {
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
	public ChannelProcessorMultiplexer getConnection() {
		return c;
	}
	@Override
	public void unshare(StreamParameters params) {
		StreamShare s=room.getShare(params.name);
		if(s==null||s.conn!=this)
		{
			throw new IllegalArgumentException();
		}
		room.removeShare(s);
		room.updateSharesToClients();
	}
	public AbstractRcomArgs getArgs() {
		return args;
	}
}
