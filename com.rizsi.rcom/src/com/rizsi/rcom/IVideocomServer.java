package com.rizsi.rcom;

public interface IVideocomServer {
	String getVersion();
	long getNanoTime();
	IVideocomConnection connect(String userName);
}
