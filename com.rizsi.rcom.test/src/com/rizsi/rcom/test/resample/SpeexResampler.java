package com.rizsi.rcom.test.resample;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.rizsi.rcom.util.UtilStream;

public class SpeexResampler {
	byte[] buffer;
	byte[] herz=new byte[8];
	ByteBuffer bb=ByteBuffer.wrap(herz).order(ByteOrder.nativeOrder());
	public InputStream startResampling(int framesamples, final InputStream original) throws Exception
	{
		buffer=new byte[framesamples*2];
		ProcessBuilder pb=new ProcessBuilder("/home/rizsi/github/rcom/speexexample/a.out");
		pb.redirectError(Redirect.INHERIT);
		Process p=pb.start();
		InputStream is=p.getInputStream();
		OutputStream os=p.getOutputStream();
		new Thread(){
			public void run() {
				try {
					while(true)
					{
						feed(original, os);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
		return is;
	}
	private void feed(InputStream original, OutputStream os) throws Exception {
		bb.clear();
		bb.putInt(8000);
		bb.putInt(8000);
		UtilStream.readFully(buffer, original, buffer.length);
		os.write(buffer);
		os.write(herz);
	}

}
