package com.rizsi.rcom.ssh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hu.qgears.commons.UtilString;
import hu.qgears.rtemplate.runtime.DummyCodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.AbstractMultiplexer;

public class UserCollectorTemplate extends RAbstractTemplatePart {
	private CharBuffer cb=CharBuffer.allocate(1);
	private byte[] bs=new byte[1];
	private class EchoEscaper
	{
		private StringBuilder str=new StringBuilder();
		public void append(String s)
		{
			for(char c: s.toCharArray())
			{
				append(c);
			}
		}
		public void append(char c)
		{
				if(validChar(c))
				{
					str.append(c);
				}else
				{
					cb.clear();
					cb.put(c);
					cb.flip();
					ByteBuffer out=StandardCharsets.UTF_8.encode(cb);
					while(out.hasRemaining())
					{
						str.append("\\\\x");
						bs[0]=out.get();
						str.append(UtilString.toHex(bs));
					}
				}
		}
		public boolean validChar(char ch)
		{
			if(ch>126)
			{
				// lower ASCII only and DEL is prohibited
				return false;
			}
			if(ch==' ')
			{
				return false;
			}
			if(ch=='-')
			{
				return true;
			}
			if(ch=='.')
			{
				return true;
			}
			if(ch<='9' && ch>='0')
			{
				return true;
			}
			if(ch<='@')
			{
				// characters below are possible escaping characters also disallowed
				return false;
			}
			return true;
		}
	}
	public UserCollectorTemplate() {
		super(new DummyCodeGeneratorContext());
	}
	public String generate(List<UserKey> keys, String host, int port) throws IOException
	{
		write("# RCom start\n");
		for(UserKey key: keys)
		{
			String line=UtilString.split(key.key, "\r\n").get(0).trim();
			EchoEscaper ee=new EchoEscaper();
			ee.append(new String(CoolRMINioRemoter.clientId, StandardCharsets.UTF_8));
			ee.append('u');
			ee.append((char)0);
			ee.append((char)0);
			ee.append((char)0);
			ee.append(key.userName);
			for(int i=key.userName.getBytes(StandardCharsets.UTF_8).length; i<AbstractMultiplexer.userNameLength; ++i)
			{
				ee.append((char)0);
			}
			write("command=\"{ echo -en ");
			writeObject(ee.str.toString());
			write(";cat; }|socat - TCP4:");
			writeObject(host);
			write(":");
			writeObject(port);
			write("\",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty ");
			writeObject(line);
			write("\n");
		}
		write("# RCom end\n");
		finishDeferredParts();
		return getTemplateState().getOut().toString();
	}
}
