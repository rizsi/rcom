package com.rizsi.rcom.gui;

import com.rizsi.rcom.util.VideoStreamProcessor;

public interface IVideoStreamContainer {
	VideoStreamProcessor getVideoStream();
	public void setGuiObject(Object o);
	public Object getGuiObject();
	/**
	 * Is this video stream closed? When closed then UI object must be deleted.
	 * @return
	 */
	boolean isClosed();
}
