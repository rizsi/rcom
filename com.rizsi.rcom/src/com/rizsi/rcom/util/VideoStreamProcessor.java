package com.rizsi.rcom.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.NullOutputStream;

import hu.qgears.commons.UtilProcess;

/**
 * Read an mpeg stream run it through ffmpeg and receive a stream of images.
 */
public class VideoStreamProcessor implements AutoCloseable
{
	private int w, h;
	private int origW, origH;
	private String enconding;
	private BufferedImage[] frames=new BufferedImage[30];
	int writePtr=0;
	private volatile int nRead;
	private Process p;
	private AbstractRcomArgs args;
	public VideoStreamProcessor(AbstractRcomArgs args, int origW, int origH, int w, int h, String encoding)
	{
		this.args=args;
		this.origW=origW;
		this.origH=origH;
		this.w=w;
		this.h=h;
		this.enconding=encoding;
		for(int i=0;i<frames.length;++i)
		{
			frames[i]=new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		}
	}
	public OutputStream launch() throws IOException
	{
		String scale="";
		if(w!=origW||h!=origH)
		{
			scale=" -vf scale="+w+":"+h+" ";
		}
		String vdeo_size=" -video_size "+origW+"x"+origH+" ";
		String trace="-v trace";
		String command=args.program_ffmpeg+" -analyzeduration 0 -fpsprobesize 0 -probesize 32000 -f "+enconding+" -i - "+scale+" -f rawvideo -pix_fmt bgr24 -";
		System.out.println("Decoder command: $ "+command);
		p=Runtime.getRuntime().exec(command);
		UtilProcess.streamErrorOfProcess(p.getErrorStream(), new NullOutputStream());
		new Thread("Read frames")
		{
			public void run() {
				readFrames(p.getInputStream());
			};
		}.start();
		return p.getOutputStream();
	}
	protected void readFrames(InputStream inputStream) {
		try {
			while(true)
			{
				BufferedImage im=frames[writePtr];
				DataBufferByte b=(DataBufferByte)im.getRaster().getDataBuffer();
				byte[] data=b.getData();
				int at=0;
				while(at<data.length)
				{
					int n=inputStream.read(data, at, data.length-at);
					if(n<0)
					{
						throw new EOFException();
					}
					at+=n;
				}
				nRead++;
				writePtr++;
				writePtr%=frames.length;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public BufferedImage getLatestImage() {
		if(nRead>0)
		{
			int idx=(nRead-1)%frames.length;
			return frames[idx];
		}
		return null;
	}
	@Override
	public void close() {
		if(p!=null)
		{
			p.destroy();
			p=null;
		}
	}
}
