package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.rizsi.rcom.util.UtilStream;

public class SpeexResampler implements AutoCloseable
{
	public interface ResampledReceiver
	{
		void receiveResampled(byte[] data, int nsamples) throws IOException;
	}
	private final ResampledReceiver receiver;
	final byte[] herz=new byte[8];
	final ByteBuffer bb=ByteBuffer.wrap(herz).order(ByteOrder.nativeOrder());
	final private OutputStream os;
	final private InputStream is;
	final private byte[] receivebuffer;
	final Process p;
	final private Pipe pis;
	public SpeexResampler(int framesamples, ResampledReceiver receiver) throws Exception
	{
		this.receiver=receiver;
		this.pis=new Pipe(framesamples*2*2);
		receivebuffer=new byte[framesamples*2];
		ProcessBuilder pb=new ProcessBuilder("/home/rizsi/github/rcom/speexexample/a.out", "resample", ""+framesamples, ""+8000);
		pb.redirectError(Redirect.INHERIT);
		p=pb.start();
		is=p.getInputStream();
		os=p.getOutputStream();
		return ;
	}
	public void feed(byte[] buffer, int sourceHz, int targetHz) throws Exception {
		pis.write(buffer, 0, buffer.length);
		while(pis.available()>0)
		{
			int n=Math.min(pis.available(), buffer.length);
			System.out.println("N bytes: "+n);
			pis.readAhead(buffer, 0, n);
			bb.clear();
			bb.putInt(n/2);
			os.write(herz, 0, 4);
			os.write(buffer, 0, n);
			bb.clear();
			bb.putInt(sourceHz);
			bb.putInt(targetHz);
			os.write(herz);
			os.flush();
			bb.clear();
			UtilStream.readFully(herz, is);
			int in_len=bb.getInt();
			int out_len=bb.getInt();
			System.out.println("Read buffer: "+in_len+" "+out_len);
			if(out_len>0)
			{
				UtilStream.readFully(receivebuffer, is, out_len*2);
				receiver.receiveResampled(receivebuffer, out_len);
			}
			pis.read(in_len*2);
		}
	}
	@Override
	public void close() {
		if(p!=null)
		{
			p.destroy();
		}
	}
}
