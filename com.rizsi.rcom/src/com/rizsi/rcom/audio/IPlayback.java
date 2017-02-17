package com.rizsi.rcom.audio;

public interface IPlayback extends AutoCloseable
{
	@Override
	void close();
}
