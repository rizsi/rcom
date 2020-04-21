package com.rizsi.rcom.gui;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.flexdock.view.View;

import com.rizsi.rcom.util.VideoStreamProcessor;

public class AnimatedGuiElement {
	ScalablePane selfVideo;
	IVideoStreamContainer source;
	public View view;

	public AnimatedGuiElement(IVideoStreamContainer source) {
		this.source=source;
		selfVideo = new ScalablePane(null);
	}

	public void update() {
		VideoStreamProcessor stream=source.getVideoStream();
		if(stream!=null)
		{
			BufferedImage im=source.getVideoStream().getCurrentImage();
			if(im!=null)
			{
				selfVideo.setImage(im);
			}else
			{
				selfVideo.setImage(null);
			}
		}else
		{
			selfVideo.setImage(null);
		}
	}

	public JPanel getUiComponent() {
		return selfVideo;
	}

	public boolean isClosed() {
		return source.isClosed();
	}
}
