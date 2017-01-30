package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MyBuffer {
	public long position=0;
	private ByteBuffer bb;
	public void writeFromChannel(SocketChannel sc) throws IOException
	{
		int n=sc.read(bb);
		position+=n;
//		sc.
	}
//	public void 
}
