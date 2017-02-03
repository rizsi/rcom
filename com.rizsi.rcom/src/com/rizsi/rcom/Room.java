package com.rizsi.rcom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
	private List<VideoConnection> conns=new ArrayList<>();
	private Map<String, StreamShare> shares=new HashMap<>();
	private VideocomServer server;
	private String name;
	
	public Room(VideocomServer server, String name) {
		super();
		this.server = server;
		this.name=name;
	}
	public void addUser(VideoConnection videoConnection) {
		synchronized (this) {
			conns.add(videoConnection);
		}
		updateUsersToClients();
		videoConnection.callbackCurrentShares(getSharesList());
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
				try
				{
					c.callbackCurrentShares(getSharesList());
				}catch(Exception e)
				{
					e.printStackTrace();
					c.dispose();
				}
			}
		}
	}
	public void updateUsersToClients()
	{
		synchronized (this) {
			List<String> users=new ArrayList<>();
			for(VideoConnection c: conns)
			{
				users.add(c.getUserName());
			}
			for(VideoConnection c: conns)
			{
				try
				{
					c.callbackCurrentUsers(users);
				}catch(Exception e)
				{
					e.printStackTrace();
					c.dispose();
				}
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

	public void removeUser(VideoConnection videoConnection) {
		System.out.println("Connection removed: "+videoConnection.getUserName());
		List<StreamShare> toDispose=new ArrayList<>();
		synchronized (server) {
			synchronized (this) {
				conns.remove(videoConnection);
				try
				{
					videoConnection.callbackCurrentShares(new ArrayList<StreamParameters>());
					videoConnection.callbackCurrentUsers(new ArrayList<String>());
				}catch(Exception e){
					// If we remove user due to doscinnect then this throws an exception
				}
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
				if(conns.size()==0)
				{
					// No further dispose is required as there is no reference to this object and all streams are closed.
					server.removeRoom(name);
				}
			}
		}
		for(StreamShare s: toDispose)
		{
			s.dispose();
		}
		updateSharesToClients();
		updateUsersToClients();
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
		System.out.println("Share removed: "+s.params.name);
		s.dispose();
	}
}
