package com.rizsi.rcom.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.rizsi.rcom.StreamSink;
import com.rizsi.rcom.cli.Client;
import com.rizsi.rcom.cli.UtilCli;
import com.rizsi.rcom.webcam.ListCams;
import com.rizsi.rcom.webcam.WebCamParameter;

public class Gui extends JFrame {
	private static final long serialVersionUID = 1L;
	Client client;
	private List<AnimatedGuiElement> animated=new ArrayList<AnimatedGuiElement>();
	public static void commandline(String[] args) throws Exception {
		GuiCliArgs a=new GuiCliArgs();
		UtilCli.parse(a, args, true);

		Client c = new Client();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Gui g = new Gui(c);
				g.setTitle("Videocom");
				g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				g.setSize(new Dimension(640, 480));
				g.setVisible(true);
			}
		});
		c.run(a);
	}

	private JPanel right;
	public Gui(Client c2) {
		this.client=c2;
		JPanel left = new JPanel();
		right = new JPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		getContentPane().add(splitPane);
		JCheckBox video = new JCheckBox("Stream webcam");
		Container c = left;
		c.setLayout(new GridLayout(0, 1));
		c.add(video);
		video.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b=video.isSelected();
				WebCamParameter p=null;
				if(b)
				{
					try {
						p=new ListCams().queryUser(c2.getArgs());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(p==null)
					{
						video.setSelected(false);
					}
				}
				client.setVideoStreamingEnabled(p);
			}
		});
		JCheckBox audio = new JCheckBox("Stream audio");
		c.add(audio);
		audio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.setAudioStreamingEnabled(audio.isSelected());
			}
		});
		JCheckBox vnc = new JCheckBox("Launch VNC");
		c.add(vnc);
		vnc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				client.setVNCShareEnabled(vnc.isSelected());
			}
		});
		right.setLayout(new FlowLayout());

		animated.add(new AnimatedGuiElement(right, client.getSelfVideo()));
		
		int delay = 1000/20; // milliseconds
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				timer();
			}
		};
		new Timer(delay, taskPerformer).start();
	}

	protected void timer() {
		for(AnimatedGuiElement anim: animated)
		{
			anim.update();
		}
		// TODO enough to sync them once every second
		// TODO remove closed ones
		for(StreamSink s:client.getStreams())
		{
			if(s instanceof IVideoStreamContainer)
			{
				IVideoStreamContainer vs=(IVideoStreamContainer) s;
				if(vs.getGuiObject()==null)
				{
					AnimatedGuiElement g=new AnimatedGuiElement(right, vs);
					vs.setGuiObject(g);
					animated.add(g);
				}
			}
		}
	}
}
