package nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * Thread that processes NIO events in a select loop.
 * Also has the capability to execute runnables from a queue.
 * This mechanism can be used to interact with the procesing thread.
 * 
 * @author rizsi
 *
 */
public class NioThread extends Thread {
	private Selector s;
	private ConcurrentLinkedQueue<Runnable> tasks=new ConcurrentLinkedQueue<>();
	/**
	 * Add a task to the task execution queue.
	 * @param task
	 */
	public void addTask(Runnable task)
	{
		tasks.add(task);
		s.wakeup();
	}
	/**
	 * Add a task to the task execution queue and return a future object that represents the result of the callable.
	 * @param task
	 * @return
	 */
	public <T> SignalFutureWrapper<T> addTask(Callable<T> task)
	{
		SignalFutureWrapper<T> ret=new SignalFutureWrapper<>();
		tasks.add(new Runnable() {
			
			@Override
			public void run() {
				try {
					Object o=task.call();
					ret.ready(o, null);
				} catch (Exception e) {
					ret.ready(null, e);
				}
			}
		});
		s.wakeup();
		return ret;
	}

	/**
	 * Create a new NIO processing thread with a {@link Selector}.
	 * @throws IOException
	 */
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
						}catch(IOException e)
						{
							ChannelProcessor p=(ChannelProcessor)key.attachment();
							try {
								key.channel().close();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								p.close(e);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}catch(Exception e)
						{
							ChannelProcessor p=(ChannelProcessor)key.attachment();
							e.printStackTrace();
							try {
								key.channel().close();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								p.close(e);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
				Runnable r;
				while((r=tasks.poll())!=null)
				{
					try {
						r.run();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
	/**
	 * Register the channel processor and channel so that it is handled by the NIO event processor thread.
	 * 
	 * Must be called from this thread. 
	 * 
	 * @param c
	 * @param interestOps
	 * @param channelProcessor
	 * @return
	 * @throws ClosedChannelException
	 */
	public SelectionKey register(SelectableChannel c, int interestOps, ChannelProcessor channelProcessor) throws ClosedChannelException {
		// TODO thread check should be an option to optimize speed
		if(Thread.currentThread()!=this)
		{
			throw new RuntimeException("Illegal thread access");
		}
		return c.register(s, interestOps, channelProcessor);
	}
}
