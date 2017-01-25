package com.rizsi.rcom;

public interface IVideocomServer {
	public static final String id="videocom";
	String getVersion();
	long getNanoTime();
	IVideocomConnection connect(String userName);
}
