package com.rizsi.rcom.gui;

import com.rizsi.rcom.util.VideoStreamProcessor;

public interface IVideoStreamContainer {
	VideoStreamProcessor getVideoStream();
	public void setGuiObject(Object o);
	public Object getGuiObject();
}
