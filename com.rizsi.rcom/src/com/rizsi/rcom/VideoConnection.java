package com.rizsi.rcom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.Slot;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

public class VideoConnection implements IVideocomConnection
{
	private DemuxedConnection c;
	private Room room;
	private int id;
	private IVideocomCallback callback;
	private GenericCoolRMIRemoter remoter;
	private String userName;
	private ExecutorService asyncCallback=Executors.newSingleThreadExecutor();
	private Map<String, StreamRegistration> registrations=new HashMap<>();
	private AbstractRcomArgs args;
	public VideoConnection(AbstractRcomArgs args, Room room, GenericCoolRMIRemoter remoter, int id, String userName) {
		this.remoter=remoter;
		this.c=(DemuxedConnection)((CoolRMIRemoter)remoter).getConnection();
		this.room=room;
		this.id=id;
		this.userName=userName;
	}
	public void init()
	{
		c.connectionclosed.addOnReadyHandler(new Slot<SignalFuture<DemuxedConnection>>() {
			
			@Override
			public void signal(SignalFuture<DemuxedConnection> value) {
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
		c.getMultiplexer().addListener(channel, share);
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
		callback.currentShares(room.getSharesList());
	}

	public void callbackMessage(String message) {
		if(callback!=null)
		{
			asyncCallback.submit(new Runnable() {
				@Override
				public void run() {
					callback.message(message);
				}
			});
		}
	}

	public void callbackCurrentShares(List<StreamParameters> values) {
		if(callback!=null)
		{
			asyncCallback.submit(new Runnable() {
				@Override
				public void run() {
					callback.currentShares(values);
				}
			});
		}		
	}
	public void dispose()
	{
		asyncCallback.shutdown();
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
	public DemuxedConnection getConnection() {
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
