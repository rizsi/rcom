package com.rizsi.rcom.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.rizsi.rcom.VideoServerTCPListener;
import com.rizsi.rcom.cli.UtilCli;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;

public class Connect {
	public void main(String[] args) throws Exception {
		ConnectArgs a=new ConnectArgs();
		UtilCli.parse(a, args, false);
		run(a);

	}
	byte[] b=new byte[1024];
	ByteBuffer bb=ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
	private void run(ConnectArgs a) throws IOException {
		a.apply();
		try(Socket s=new Socket(a.host, a.port))
		{
			OutputStream os=s.getOutputStream();
			os.write(VideoServerTCPListener.clientID.getBytes(StandardCharsets.UTF_8));
			os.write('u');
			byte[] user=a.user.getBytes(StandardCharsets.UTF_8);
			bb.putInt(user.length);
			os.write(b, 0, 4);
			os.write(user);
			ConnectStreams.startStreamThread(s.getInputStream(), System.out, true, UtilFile.defaultBufferSize.get());
			ConnectStreams.doStream(System.in, os, true, UtilFile.defaultBufferSize.get());
		}
	}
}
