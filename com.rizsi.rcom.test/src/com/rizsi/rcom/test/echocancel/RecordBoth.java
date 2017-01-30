package com.rizsi.rcom.test.echocancel;

import java.io.ByteArrayOutputStream;

import com.rizsi.rcom.NullOutputStream;

public class RecordBoth 
{
	SpeexEchoCancel echo;
	Mic m;
	ByteArrayOutputStream raw=new ByteArrayOutputStream();
	ByteArrayOutputStream echoCancelled=new ByteArrayOutputStream();
	public RecordBoth(SpeexEchoCancel echo, Mic m) {
		this.m=m;
		this.echo=echo;
		echo.setProcessed(echoCancelled);
		m.setRecord(raw);
	}
	public void stop()
	{
		echo.setProcessed(new NullOutputStream());
		m.setRecord(null);
	}
}