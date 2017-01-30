package com.rizsi.rcom.test.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class NioThread extends Thread {
	Selector s;

	public NioThread() throws IOException {
		s = SelectorProvider.provider().openSelector();
	}

	@Override
	public void run() {
		try {
			while (true) {
				int n = s.select();
				if (n > 0) {
					Iterator<SelectionKey> iter = s.selectedKeys().iterator();
					while (iter.hasNext()) {
						SelectionKey key = (SelectionKey) iter.next();
						iter.remove();
						int ops=key.readyOps();
						try
						{
							if(!key.isValid())
							{
								ChannelProcessor p=(ChannelProcessor)key.attachment();
								try {
									p.keyInvalid(key);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								key.cancel();
							}
							if((ops&SelectionKey.OP_ACCEPT)!=0)
							{
								ChannelProcessor p=(ChannelProcessor)key.attachment();
								p.accept(key);
							}
							if((ops&SelectionKey.OP_WRITE)!=0)
							{
								ChannelProcessor p=(ChannelProcessor)key.attachment();
								p.write(key);
							}
							if((ops&SelectionKey.OP_READ)!=0)
							{
								ChannelProcessor p=(ChannelProcessor)key.attachment();
								p.read(key);
							}
							if((ops&SelectionKey.OP_CONNECT)!=0)
							{
								ChannelProcessor p=(ChannelProcessor)key.attachment();
								p.connect(key);
							}
						}catch(Exception e)
						{
							try {
								key.channel().close();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
