package com.rizsi.rcom.cli;

import java.io.IOException;

import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.IVideocomServer;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.VideoServerTCPListenerFactory;
import com.rizsi.rcom.VideocomServer;
import com.rizsi.rcom.ssh.UserCollector;

import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;

public class Server {
	public void main(String[] args) throws Exception {
		ServerCliArgs a=new ServerCliArgs();
		UtilCli.parse(a, args, true);
		new Server().run(a);
	}

	private void run(ServerCliArgs a) throws IOException {
		if(a.authFile!=null&&a.connectCommand!=null&&a.keyDir!=null)
		{
			new UserCollector(a.keyDir, a.authFile, a.connectCommand).start();
		}
		if(!a.disableServer)
		{
			CoolRMIServer srv=new CoolRMIServer(Launcher.class.getClassLoader(), new VideoServerTCPListenerFactory(a), false);
			srv.getServiceRegistry().addProxyType(VideoConnection.class, IVideocomConnection.class);
			srv.getServiceRegistry().addService(new CoolRMIService(IVideocomServer.id, IVideocomServer.class, new VideocomServer()));
			srv.start();
		}
	}
}
