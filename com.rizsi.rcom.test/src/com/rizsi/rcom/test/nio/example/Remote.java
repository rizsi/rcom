package com.rizsi.rcom.test.nio.example;

import java.util.concurrent.Callable;

import hu.qgears.commons.UtilTimer;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.coolrmi.messages.CoolRMICall;

public class Remote implements Iremote
{
	@Override
	public String getValue(String s) {
		System.out.println("RECV: "+s);
		CoolRMICall call=CoolRMICall.getCurrentCall();
		SignalFutureWrapper<Object> ret=UtilTimer.getInstance().executeTimeout(1000, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return "REPLY: "+s;
			}
		});
		call.createAsyncReply(ret);
		return null;
	}
}
