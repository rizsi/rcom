package com.rizsi.rcom.gui;

import com.rizsi.rcom.util.VideoStreamProcessor;

public class DelegateVideoStreamContainer implements IVideoStreamContainer {
	private volatile IVideoStreamContainer delegate;
	public void setDelegate(IVideoStreamContainer delegate) {
		this.delegate=delegate;
	}
	@Override
	public VideoStreamProcessor getVideoStream() {
		IVideoStreamContainer d=delegate;
		if(d==null)
		{
			return null;
		}
		return d.getVideoStream();
	}
	private Object guiObject;
	@Override
	public void setGuiObject(Object o) {
		this.guiObject=o;
	}
	@Override
	public Object getGuiObject() {
		return guiObject;
	}	
}
