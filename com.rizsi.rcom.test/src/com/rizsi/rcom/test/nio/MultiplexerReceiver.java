package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

abstract public class MultiplexerReceiver {
	abstract public void startMessage(int length);

	abstract public void read(SelectionKey key, ReadableByteChannel bc) throws IOException;
}
