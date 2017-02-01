package com.rizsi.rcom.cli;

import java.net.InetSocketAddress;

import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.IVideocomServer;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.VideocomServer;
import com.rizsi.rcom.ssh.UserCollector;

import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import nio.NioThread;
import nio.coolrmi.CoolRMINioServer;

public class Server {
	public void main(String[] args) throws Exception {
		ServerCliArgs a=new ServerCliArgs();
		UtilCli.parse(a, args, true);
		new Server().run(a);
	}

	private void run(ServerCliArgs a) throws Exception {
		a.apply();
		if(a.authFile!=null&&a.connectCommand!=null&&a.keyDir!=null)
		{
			new UserCollector(a.keyDir, a.authFile, a.connectCommand, a.beforeKeyDirUpdateCommand, a.keyDirUpdateTimeoutMillis).start();
		}
		if(!a.disableServer)
		{
			NioThread nt=new NioThread();
			CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
			reg.addProxyType(VideoConnection.class, IVideocomConnection.class);
			reg.addService(new CoolRMIService(IVideocomServer.id,
					IVideocomServer.class, new VideocomServer(a)));
			CoolRMINioServer srv=new CoolRMINioServer(getClass().getClassLoader(), reg);
			srv.listen(nt, new InetSocketAddress(a.host, a.port));
			nt.start();
		}
	}
}
