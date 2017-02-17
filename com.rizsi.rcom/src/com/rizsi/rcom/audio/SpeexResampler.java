package com.rizsi.rcom.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.util.UtilStream;

import hu.qgears.commons.signal.SignalFutureWrapper;

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
	final private int framesamples;
	public SpeexResampler(AbstractRcomArgs args, int framesamples, ResampledReceiver receiver) throws Exception
	{
		this.receiver=receiver;
		this.framesamples=framesamples;
		this.pis=new Pipe(framesamples*2*2);
		receivebuffer=new byte[framesamples*2];
		ProcessBuilder pb=new ProcessBuilder(args.program_speexcmd, "resample", ""+framesamples, ""+8000);
		pb.redirectError(Redirect.INHERIT);
		p=pb.start();
		is=p.getInputStream();
		os=p.getOutputStream();
		checkSpeexCmdVersion(p, is);
		return;
	}
	public static void checkSpeexCmdVersion(Process p, final InputStream is) {
		String req="speexcmd 0.0.5 for RCOM:";
		final byte[] reqb=req.getBytes(StandardCharsets.UTF_8);
		final byte[] recv=new byte[reqb.length];
		final SignalFutureWrapper<Boolean> checkresult=new SignalFutureWrapper<>();
		new Thread("Wait for speexcmd version string"){public void run() {
			try {
				UtilStream.readFully(recv, is);
				checkresult.ready(true, null);
			} catch (IOException e) {
				checkresult.ready(false, e);
			}
		};}.start();
		try {
			checkresult.get(3, TimeUnit.SECONDS);
		} catch (Exception e) {
			p.destroy();
			throw new RuntimeException("Invalid version of speexcmd. Required: '"+req+"'", e);
		}
		if(!Arrays.equals(reqb, recv))
		{
			p.destroy();
			throw new RuntimeException("Invalid version of speexcmd. Required: "+req);
		}
	}
	public void feed(byte[] buffer, int sourceHz, int targetHz) throws Exception {
		pis.write(buffer, 0, buffer.length);
		while(pis.available()>=framesamples*2)
		{
			int n=Math.min(pis.available(), buffer.length);
			//System.out.println("Send n samples: "+n/2);
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
			if(out_len>0)
			{
				UtilStream.readFully(receivebuffer, is, out_len*2);
				receiver.receiveResampled(receivebuffer, out_len);
			}
			// System.out.println("Received n samples: "+out_len+" processed: "+in_len+" fed: "+n/2);
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
