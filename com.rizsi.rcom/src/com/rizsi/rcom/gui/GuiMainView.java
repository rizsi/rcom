package com.rizsi.rcom.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.flexdock.view.View;

import com.rizsi.rcom.StreamParameters;
import com.rizsi.rcom.StreamSink;
import com.rizsi.rcom.cli.Client;
import com.rizsi.rcom.cli.Client.IListener;
import com.rizsi.rcom.webcam.ListCams;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.CompatFunction;
import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilString;

/**
 * Main view is a dockable with main controls.
 */
public class GuiMainView implements IListener {
	Client client;
	private List<AnimatedGuiElement> animated=new ArrayList<AnimatedGuiElement>();
	private int idCounter=0;

	protected void launchClient() {
		connect.setEnabled(false);
		connect.setText("Connecting...");
		disconnect.setEnabled(true);
		disconnect.setText("Disconnect from "+a.connectionString);
		client=new Client();
		new Thread("Client mainThread")
		{
			public void run() {
				try {
					client.run(a, GuiMainView.this);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}
		.start();
	}

	private JButton connect;
	private JButton disconnect;
	private JButton room;
	private JTextArea users;
	private JTextArea shares;
	private JTextArea chat;
	private JTextField message;
	private List<AbstractButton> buttons=new ArrayList<>();
	private GuiCliArgs a;
	private Gui2 gui;
	private View view;
	public GuiMainView(GuiCliArgs a, Gui2 gui) {
		this.a=a;
		this.gui=gui;
		view=new View("Connection", "Connection", "Connection");
		//view.addAction(View.CLOSE_ACTION);
		view.addAction(View.PIN_ACTION);

		JPanel left = new JPanel();
		view.setContentPane(left);
		
		connect=new JButton("Connect...");
		disconnect=new JButton("Disconnect");
		disconnect.setEnabled(false);
		room=new JButton("No room selected");
		room.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				roomEnter();
			}
		});
		left.add(room);
		left.add(new JLabel("users:"));
		users=new JTextArea(25, 15);
		users.setEditable(false);
		left.add(users);
		left.add(new JLabel("Shares:"));
		shares=new JTextArea(25, 15);
		shares.setEditable(false);		
		left.add(shares);
		left.add(new JLabel("Chat:"));
		chat=new JTextArea(25, 15);
		chat.setEditable(false);
		JScrollPane sp=new JScrollPane(chat);
		left.add(sp);
		message=new JTextField();
		message.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(client!=null)
				{
					client.sendMessage(message.getText());
				}
				message.setText("");
			}
		});
		message.setEnabled(false);
		left.add(message);
		left.add(connect);
		left.add(disconnect);
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		disconnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				client.close();
			}
		});
		final JCheckBox video = new JCheckBox("Stream webcam");
		buttons.add(video);
		Container c = left;
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(video);
		video.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b=video.isSelected();
				WebCamParameter p=null;
				if(b)
				{
					try {
						p=new ListCams().queryUser(client.getArgs());
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
		final JCheckBox audio = new JCheckBox("Stream audio");
		buttons.add(audio);
		c.add(audio);
		audio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.setAudioStreamingEnabled(audio.isSelected());
			}
		});
		if(a.enableVNC)
		{
			final JCheckBox vnc = new JCheckBox("Launch VNC");
			c.add(vnc);
			buttons.add(vnc);
			vnc.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					client.setVNCShareEnabled(vnc.isSelected());
				}
			});
		}
		int delay = 1000/20; // milliseconds
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				timer();
			}
		};
		new Timer(delay, taskPerformer).start();
		disableButtons();
	}
	protected void connect() {
		String connectionstring = JOptionPane.showInputDialog(gui, "Server connection string", a.connectionString==null?"":a.connectionString);
		a.connectionString=connectionstring;
		launchClient();
	}

	protected void roomEnter() {
		String connectionstring = JOptionPane.showInputDialog(gui, "Enter room", a.room==null?"":a.room);
		a.room=connectionstring;
		
		client.enterRoom(a.room);
	}
	private long lastSynced;
	protected void timer() {
		for(AnimatedGuiElement anim: animated)
		{
			anim.update();
		}
		long t=System.currentTimeMillis();
		if(Math.abs(t-lastSynced)>1000)
		{
			lastSynced=t;
			// TODO remove closed ones
			Client c=client;
			if(c!=null)
			{
				for(StreamSink s:c.getStreams())
				{
					if(s instanceof IVideoStreamContainer)
					{
						IVideoStreamContainer vs=(IVideoStreamContainer) s;
						if(vs.getGuiObject()==null)
						{
							AnimatedGuiElement g=new AnimatedGuiElement(vs);
							vs.setGuiObject(g);
							showWindow(g);
						}
					}
				}
			}
		}
	}
	private void showWindow(AnimatedGuiElement g) {
		animated.add(g);
		int index=idCounter++;
		View v=new View("Video"+index, "Video"+index, "Video"+index);
		v.addAction(View.PIN_ACTION);
		JPanel p=new JPanel();
		p.setLayout(new FlowLayout());
		p.add(g.getUiComponent());
		v.setContentPane(p);
		gui.showView(v);
	}
	@Override
	public void connected(final Client client) {
		this.client=client;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				connect.setEnabled(false);
				connect.setText("Connected");
				AnimatedGuiElement anim=new AnimatedGuiElement(client.getSelfVideo());
				showWindow(anim);
				room.setEnabled(true);
			}
		});
	}
	@Override
	public void closed(Client c) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				disableButtons();
				connect.setEnabled(true);
				connect.setText("Connect...");
				room.setText("No room");
				disconnect.setEnabled(false);
				client=null;
				room.setEnabled(false);
				message.setEnabled(false);
			}
		});
	}
	protected void disableButtons() {
		for(AbstractButton b: buttons)
		{
			b.setEnabled(false);
			b.setSelected(false);
		}
	}

	@Override
	public void roomEntered(Client client, final String name) {
		if(client==this.client)
		{
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					room.setEnabled(true);
					for(AbstractButton b: buttons)
					{
						b.setSelected(false);
					}
					if(name!=null)
					{
						room.setText("Room: "+name);
						for(AbstractButton b: buttons)
						{
							b.setEnabled(true);
						}
					}else
					{
						room.setText("Enter room...");
						for(AbstractButton b: buttons)
						{
							b.setEnabled(false);
						}
					}
					chat.setText("");
					message.setEnabled(name!=null);
				}
			});
		}
	}

	@Override
	public void usersUpdated(final List<String> userNames) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				users.setText(UtilString.concat(userNames, "\n"));
			}
		});
	}

	@Override
	public void updateShares(final List<StreamParameters> arrayList) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				shares.setText(UtilString.concat(arrayList, new UtilComma("\n"), new CompatFunction<StreamParameters, String>() {
					@Override
					public String apply(StreamParameters t) {
						return t.name;
					}
				}));
			}
		});
	}

	@Override
	public void messageReceived(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				chat.setText(chat.getText()+"\n"+message);
				// TODO Notify user if application is minimized?
			}
		});
	}
	public View getView() {
		return view;
	}
}
