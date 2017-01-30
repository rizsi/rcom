package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

import hu.qgears.commons.UtilFile;

public class MultiplexerInputDevNull extends MultiplexerReceiver
{
	private int length;
	private static ByteBuffer devNull=ByteBuffer.allocateDirect(UtilFile.defaultBufferSize.get());
	private ByteBuffer bb=devNull.duplicate();
	
	@Override
	public void startMessage(int length) {
		
		// TODO Auto-generated method stub
		
	}
	@Override
	public void read(SelectionKey key, ReadableByteChannel bc) throws IOException {
		bb.position(0);
		bb.limit(Math.min(length, bb.capacity()));
		bc.read(bb);
	}

}
