package nio.multiplexer;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * A sending endpoint of the channel multiplexer.
 * 
 * Subclasses must implement the abstract methods and call the dataAvailable() method when
 * getAvailable bytes change from 0 to positive value.
 * 
 * @author rizsi
 *
 */
abstract public class MultiplexerSender {
	private final IMultiplexer multiplexer;
	private int id;
	public MultiplexerSender(IMultiplexer multiplexer) {
		super();
		this.multiplexer = multiplexer;
	}
	public void register()
	{
		multiplexer.register(this);
	}
	/**
	 * Notify the NIO thread that the can write state has to be updated.
	 */
	public void dataAvailable()
	{
		multiplexer.dataAvailable(this);
	}
	/**
	 * 
	 * @param key
	 * @param channel
	 * @param sendCurrentLength
	 * @return number of bytes written to the target Must be positive. 0 means error and this sender is going to be closed.
	 */
	abstract public int send(WritableByteChannel channel, int sendCurrentLength) throws IOException;
	public int getId() {
		return id;
	}
	abstract public int getAvailable();
	final protected void setId(int id) {
		this.id=id;
	}
	public void close(Exception e)
	{
		multiplexer.remove(this);
	}
	/**
	 * The length of the remote receiver buffer has changed.
	 * @param receiverAvailable
	 */
	abstract public void receiveBufferAvailable(long receiverAvailable);
}
