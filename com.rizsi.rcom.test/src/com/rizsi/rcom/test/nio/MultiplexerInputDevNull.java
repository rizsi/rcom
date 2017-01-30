package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

import hu.qgears.commons.UtilFile;

public class MultiplexerInputDevNull extends MultiplexerReceiver
{
	private static ByteBuffer devNull=ByteBuffer.allocateDirect(UtilFile.defaultBufferSize.get());
	private ByteBuffer bb=devNull.duplicate();
	
	@Override
	public int read(SelectionKey key, ReadableByteChannel bc, int maxLen) throws IOException {
		bb.position(0);
		bb.limit(Math.min(maxLen, bb.capacity()));
		int n=bc.read(bb);
		return n;
	}
}
