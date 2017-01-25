package com.rizsi.rcom;

public interface IVideocomConnection {
	/**
	 * Share a stream with the room.
	 * @param i identifier of the send stream in the multiplexer
	 * @param params parameters of the shared stream (or object)
	 * @return TODO
	 */
	IStreamData shareStream(int i, StreamParameters params);
	void sendMessage(String message);
	void registerCallback(IVideocomCallback callback);
	int getId();
	/**
	 * Register a listener for the stream with the given id.
	 * @param name
	 * @param channel id of the channel from client to server. -1 means no channel.
	 * @return the stream returned by the server is not activated yet. The launchStream call will activate it.
	 */
	IStreamData registerStream(String name, int channel);
	/**
	 * The client marks that it has already set up the listener to the retrieved channel object
	 * so the server can already send the data to it.
	 * @param name
	 * @param channel
	 */
	void launchStream(String name);
	void unregisterStream(String s);
	void unshare(StreamParameters params);
}
