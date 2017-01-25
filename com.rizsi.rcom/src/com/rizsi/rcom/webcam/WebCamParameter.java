package com.rizsi.rcom.webcam;

public class WebCamParameter {
	private int w,h;
	private String device;
	
	public WebCamParameter(int w, int h, String device) {
		super();
		this.w = w;
		this.h = h;
		this.device = device;
	}

	@Override
	public String toString() {
		return ""+device+" "+w+"x"+h;
	}
	public int getW() {
		return w;
	}
	public int getH() {
		return h;
	}
	public String getDevice() {
		return device;
	}
}
