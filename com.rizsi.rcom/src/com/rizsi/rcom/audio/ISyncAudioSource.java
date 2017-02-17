package com.rizsi.rcom.audio;

import java.io.IOException;

/**
 * Audio stream source that is already synchronized with the output.
 * Reading the samples must immediately return a block of data.
 * @author rizsi
 *
 */
public interface ISyncAudioSource extends AutoCloseable
{
	/**
	 * Read data from the source.
	 * @param data the whole buffer must be filled with valid data.
	 * @throws IOException
	 */
	void readOutput(byte[] data) throws IOException;
	@Override
	void close();
	boolean isClosed();
}
