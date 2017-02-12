package nio.multiplexer;

import java.nio.ByteBuffer;

/**
 * Buffer that automatically grows when necessary.
 * @author rizsi
 *
 */
public class AutoGrowBuffer {
	
	private ByteBuffer data=ByteBuffer.allocate(1024).order(AbstractMultiplexer.order);
	private int writePtr;
	private int readPtr;
	private ByteBuffer readView;
	private ByteBuffer tmp=ByteBuffer.allocate(8).order(AbstractMultiplexer.order);
	public AutoGrowBuffer() {
		readView=data.asReadOnlyBuffer();
	}

	public synchronized int readSize() {
		return (writePtr-readPtr+data.capacity())%data.capacity();
	}
	public synchronized void putInt(int id) {
		ensureWriteCapacity(4);
		tmp.clear();
		tmp.putInt(id);
		tmp.flip();
		addDataFrom(tmp);
	}

	private synchronized void ensureWriteCapacity(int i) {
		int size=readSize();
		int writeCapacity=data.capacity()-size-1;
		while(writeCapacity<i)
		{
			ByteBuffer doubled=ByteBuffer.allocate(data.capacity()*2).order(AbstractMultiplexer.order);
			copyTo(doubled, size);
			readView=doubled.asReadOnlyBuffer();
			readPtr=0;
			writePtr=size;
			data=doubled;
			size=readSize();
			writeCapacity=data.capacity()-size-1;
		}
	}

	public synchronized void putLong(long avail) {
		ensureWriteCapacity(8);
		tmp.clear();
		tmp.putLong(avail);
		tmp.flip();
		addDataFrom(tmp);
	}
	private synchronized void addDataFrom(ByteBuffer b) {
		data.position(writePtr);
		int k=b.remaining();
		writePtr+=k;
		writePtr%=data.capacity();
		int n=Math.min(k, data.capacity()-data.position());
		b.limit(b.position()+n);
		data.put(b);
		k-=n;
		if(k>0)
		{
			b.limit(b.position()+k);
			data.clear();
			data.put(b);
		}
	}

	/**
	 * Copy n bytes from this round buffer to the target buffer.
	 * @param sendBuffer
	 * @param n
	 */
	public synchronized void copyTo(ByteBuffer sendBuffer, int n) {
		readView.position(readPtr);
		int k=Math.min(readView.capacity()-readPtr, n);
		readView.limit(readPtr+k);
		sendBuffer.put(readView);
		n-=k;
		readPtr+=k;
		readPtr=readPtr%readView.capacity();
		if(n>0)
		{
			readView.position(0);
			readView.limit(n);
			sendBuffer.put(readView);
			readPtr+=n;
		}
	}
}
