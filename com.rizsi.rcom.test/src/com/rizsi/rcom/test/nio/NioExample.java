package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

public class NioExample {
	public static void main(String[] args) throws IOException {
		SocketChannel sc=SocketChannel.open();
		System.out.println(sc.getOption(StandardSocketOptions.SO_RCVBUF));
		sc.setOption(StandardSocketOptions.SO_RCVBUF, 43690000);
		System.out.println(sc.getOption(StandardSocketOptions.SO_RCVBUF));
		System.out.println(sc.supportedOptions());
		new ProcessBuilder().command("alma", "korte", "szolo", "szilva").redirectError(Redirect.INHERIT).environment().put("alma", "korte");
//		sc.configureBlocking(false);
//		sc.
//		SelectionKey key=null;
//		//key.channel();
//		key.isWritable();
	}
}
