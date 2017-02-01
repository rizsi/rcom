package nio.coolrmi;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import nio.NioThread;

public class CoolRMINioClient extends CoolRMINioRemoter
{
	public CoolRMINioClient(ClassLoader classLoader, boolean guaranteeOrdering) {
		super(classLoader, guaranteeOrdering, false);
	}

	public void connect(NioThread nt, SocketAddress address) throws Exception
	{
		SocketChannel sc=SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(address);
		connect(nt, sc, true);
	}
}
