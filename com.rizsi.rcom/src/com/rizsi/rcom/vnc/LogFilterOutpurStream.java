package com.rizsi.rcom.vnc;

import java.io.EOFException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LogFilterOutpurStream extends FilterOutputStream
{

	public LogFilterOutpurStream(OutputStream out) throws IOException {
		super(out);
		pis.connect(pos);
		new Thread("Parse VNC"){
			public void run() {
				parseProtocol();
			};
		}.start();
	}
	byte[] b=new byte[1024];
	ByteBuffer bb=ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
	protected void parseProtocol() {
		try {
			byte[] v=new byte[12];
			readFully(v, pis);
			System.out.println("Version parsed: "+new String(v));
			readFully(v, pis);
			System.out.println("2nd: "+new String(v));
			int n=pis.read();
			System.out.println("Selected auth: "+n);
			// Dunno why
			pis.read();
			pis.read();
			pis.read();
			wh:
			while(true)
			{
				int code=pis.read();
				switch (code) {
				case 0:
					for(int i=1;i<20;++i)
					{
						System.out.println("Pixelformat byte: "+i+" "+pis.read());
					}
					break;
				case 2:
					// Padding
					pis.read();
					readFully(b, pis, 2);
					bb.clear();
					int nEncoding=bb.getShort();
					for(int i=0;i<nEncoding;++i)
					{
						readFully(b, pis, 4);
						bb.clear();
						System.out.println("SetEncoding value: "+i+" "+bb.getInt());
					}
					break;
				case 3:
					// Update request event
					readFully(b, pis, 9);
					break;
				case 4:
					// Key event
					readFully(b, pis, 7);
					break;
				case 5:
					// Pointer event
					readFully(b, pis, 5);
					break;
				case 6:
					// Cut text
					readFully(b, pis, 7);
					bb.clear();
					bb.get();
					bb.get();
					bb.get();
					int length=bb.getInt();
					for(int i=0;i<length;++i)
					{
						bb.get();
					}
					break;
				default:
					System.out.println("unhandled code: "+code);
					break wh;
				}
			}
			int i=0;
			while(true)
			{
				v=new byte[1];
				readFully(v, pis);
				System.out.println("Byte: "+i+" "+(int)v[0]);
				i++;
			}
//			v=new byte[20];
//			readFully(v, pis);
//			if(v[0]!=0)
//			{
//				throw new RuntimeException();
//			}
//			System.out.println("Pixelformat sent");
//			System.out.println("N auth: "+pis.read());
//			System.out.println("N auth: "+pis.read());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readFully(byte[] bs, PipedInputStream i) throws IOException {
		int at=0;
		while(at<bs.length)
		{
			int n=i.read(bs, at, bs.length-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
	private void readFully(byte[] bs, PipedInputStream i, int k) throws IOException {
		int at=0;
		while(at<k)
		{
			int n=i.read(bs, at, k-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		pos.write(b, off, len);
		super.write(b, off, len);
	}
	PipedInputStream pis=new PipedInputStream();
	PipedOutputStream pos=new PipedOutputStream();
	@Override
	public void write(int b) throws IOException {
		pos.write(b);
		super.write(b);
	}
	@Override
	public void write(byte[] b) throws IOException {
		pos.write(b);
		super.write(b);
	}
	@Override
	public void flush() throws IOException {
		pos.flush();
		super.flush();
	}
}
