package nio.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hu.qgears.commons.UtilFile;
import hu.qgears.coolrmi.streams.IConnection;

/**
 * Create a duplex input/output stream connection through a NIO channel.
 * @author rizsi
 *
 */
public class DuplexNioConnection implements IConnection
{
	InputStreamReceiver is;
	OutputStreamSender os;
	public DuplexNioConnection(ChannelProcessorMultiplexer m) throws IOException {
		is=new InputStreamReceiver(UtilFile.defaultBufferSize.get()*8, true);
		os=new OutputStreamSender(m, UtilFile.defaultBufferSize.get()*8, true);
		is.register(m, 0);
	}
	@Override
	public InputStream getInputStream() throws IOException {
		return is.in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os.os;
	}

	@Override
	public void close() throws IOException {
		is.close(null);
		os.close(null);
	}
}
