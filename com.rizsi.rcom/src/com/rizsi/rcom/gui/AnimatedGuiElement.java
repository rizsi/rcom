package com.rizsi.rcom.gui;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.rizsi.rcom.util.VideoStreamProcessor;

public class AnimatedGuiElement {
	JLabel selfVideo;
	IVideoStreamContainer source;

	public AnimatedGuiElement(IVideoStreamContainer source) {
		this.source=source;
		selfVideo = new JLabel();
	}

	public void update() {
		VideoStreamProcessor stream=source.getVideoStream();
		if(stream!=null)
		{
			BufferedImage im=source.getVideoStream().getCurrentImage();
			if(im!=null)
			{
				selfVideo.setIcon(new ImageIcon(im));
			}else
			{
				selfVideo.setIcon(null);
			}
		}else
		{
			selfVideo.setIcon(null);
		}
	}

	public Component getUiComponent() {
		return selfVideo;
	}
}
