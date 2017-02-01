package nio.coolrmi;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import nio.AbstractSocketAcceptor;
import nio.NioThread;

public class CoolRMINioServer {
	private CoolRMIServiceRegistry reg;
	private ClassLoader classLoader;
	public class SA extends AbstractSocketAcceptor
	{
		public SA(NioThread t, ServerSocketChannel c) {
			super(t, c);
		}

		@Override
		protected void socketChannelAccepted(SocketChannel sc) throws IOException {
			sc.configureBlocking(false);
			CoolRMINioRemoter srv=new CoolRMINioRemoter(classLoader, false, true);
			srv.setServiceRegistry(reg);
			try {
				srv.connect(t, sc, false);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		@Override
		public void close(Exception e) {
			if(e!=null)
			{
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
		}
	}
	
	public CoolRMINioServer(ClassLoader classLoader, CoolRMIServiceRegistry reg) {
		super();
		this.classLoader = classLoader;
		this.reg = reg;
	}

	public SA listen(NioThread nt, SocketAddress address) throws Exception
	{
		ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(address);
		SA sa=new SA(nt, ssc);
		sa.start();
		return sa;
	}
}
