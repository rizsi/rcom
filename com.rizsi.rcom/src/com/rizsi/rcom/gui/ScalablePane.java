package com.rizsi.rcom.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Scalable image plane. Resize is done on the fly when drawing because it is optimized for video.
 */
public class ScalablePane extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image master;

	public ScalablePane(Image master) {
		this.master = master;
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		if (w > 0 && h > 0 && master != null) {
			int imW = master.getWidth(null);
			int imH = master.getHeight(null);
			double ratioWin = ((double) w) / h;
			double ratioIm = ((double) imW) / imH;
			Graphics2D g2 = (Graphics2D) g;
			Object prev = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			try {
				if (ratioWin > ratioIm) {
					int dw = (int) (h * ratioIm);
					g2.drawImage(master, (w - dw) / 2, 0, dw, h, null);
				} else {
					int dh = (int) (w / ratioIm);
					g2.drawImage(master, 0, (h - dh) / 2, w, dh, null);
				}
			} finally {
				if (prev != null && prev!=RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
				}
			}
		}
	}

	public void setImage(BufferedImage im) {
		if (im != master) {
			master = im;
			invalidate();
			repaint();
		} else if (im != null) {
			invalidate();
			repaint();
		}
	}
}