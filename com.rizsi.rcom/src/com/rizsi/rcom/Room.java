package com.rizsi.rcom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

public class Room {
	private int nClient;
	private List<VideoConnection> conns=new ArrayList<>();
	private Map<String, StreamShare> shares=new HashMap<>();
	private VideocomServer server;
	
	public Room(VideocomServer server) {
		super();
		this.server = server;
	}

	public IVideocomConnection connect(GenericCoolRMIRemoter remoter, String userName) {
		VideoConnection ret;
		synchronized (this) {
			ret=new VideoConnection(server.getArgs(), this, remoter, nClient++, userName);
			conns.add(ret);
		}
		ret.init();
		return ret;
	}

	public void messageReceived(VideoConnection videoConnection, String message) {
		synchronized (this) {
			for(VideoConnection c: conns)
			{
				c.callbackMessage(message);
			}
		}
	}

	public void addShare(StreamShare share) {
		synchronized (this) {
			if(shares.containsKey(share.params.name))
			{
				throw new RuntimeException("Stream already shared: "+share.params.name);
			}
			shares.put(share.params.name, share);
			updateSharesToClients();
		}
	}
	
	void updateSharesToClients()
	{
		synchronized (this) {
			for(VideoConnection c: conns)
			{
				c.callbackCurrentShares(getSharesList());
			}
		}
	}

	public List<StreamParameters> getSharesList() {
		synchronized (this) {
			List<StreamParameters> ret=new ArrayList<>(shares.size());
			for(StreamShare s: shares.values())
			{
				ret.add(s.params);
			}
			return ret;
		}
	}

	public void remove(VideoConnection videoConnection) {
		List<StreamShare> toDispose=new ArrayList<>();
		synchronized (this) {
			conns.remove(videoConnection);
			for(StreamShare s: shares.values())
			{
				if(s.conn==videoConnection)
				{
					toDispose.add(s);
				}
			}
			for(StreamShare s: toDispose)
			{
				shares.remove(s.params.name);
			}
		}
		for(StreamShare s: toDispose)
		{
			s.dispose();
		}
		updateSharesToClients();
	}

	public StreamShare getShare(String name) {
		synchronized (this) {
			return shares.get(name);
		}
	}

	public void removeShare(StreamShare s) {
		synchronized (this) {
			shares.remove(s.params.name);
		}
		s.dispose();
	}
}
