package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;

public interface IChannelReader {

	/**
	 * The listener must read ecaxtly length bytes from the stream.
	 * @param is
	 * @param len
	 * @throws IOException 
	 */
	void readFully(InputStream is, int len) throws IOException;
}
