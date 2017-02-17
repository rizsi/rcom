package com.rizsi.rcom.audio;

public interface ICapture extends AutoCloseable
{
	@Override
	void close();
}
