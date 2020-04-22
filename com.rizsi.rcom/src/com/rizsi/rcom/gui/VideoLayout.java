package com.rizsi.rcom.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * Layout video streams
 */
public class VideoLayout implements LayoutManager
{

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(300, 480);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(15, 15);
	}

	@Override
	public void layoutContainer(Container parent) {
		Component[] cs=parent.getComponents();
		if(cs!=null)
		{
			int n=cs.length;
			int w=parent.getWidth();
			int h=parent.getHeight();
			int nw=1;
			int nh=1;
			while(n>nw*nh)
			{
				if(nh>nw)
				{
					nw++;
				}else
				{
					nh++;
				}
			}
			int i=0;
			int wc=w/nw;
			int hc=h/nh;
			for(int iw=0;iw<nw;++iw)
			{
				for(int ih=0;ih<nh;++ih)
				{
					if(cs.length>i)
					{
						Component c=cs[i];
						int x=wc*iw;
						int y=hc*ih;
						c.setBounds(x, y, wc, hc);
					}
					i++;
				}
			}
		}
	}
}
