package com.rizsi.rcom;

abstract public class StreamShare {
	public final StreamParameters params;
	public final VideoConnection conn;
	public StreamShare(VideoConnection videoConnection, StreamParameters params) {
		this.params=params;
		conn=videoConnection;
	}
	/**
	 * Register a client to this share.
	 * @param videoConnection
	 * @param channel 
	 * @return registration object that can be used to dispose the registration.
	 */
	abstract public StreamRegistration registerClient(VideoConnection videoConnection, int channel);

	abstract public void dispose();

	/**
	 * Stream data returned to the client about this share.
	 * @return
	 */
	abstract public IStreamData getStreamData();
}
