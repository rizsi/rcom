package com.rizsi.rcom;

public interface StreamRegistration extends AutoCloseable 
{
	@Override
	void close();

	IStreamData getData();

	void launch();
}
