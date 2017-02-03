package com.rizsi.rcom.cli;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetSocketAddress;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.ChannelMultiplexer.ChannelOutputStream;
import com.rizsi.rcom.IVideocomCallback;
import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.IVideocomServer;
import com.rizsi.rcom.StreamParameters;
import com.rizsi.rcom.StreamSink;
import com.rizsi.rcom.StreamSourceAudio;
import com.rizsi.rcom.StreamSourceVideoWebCam;
import com.rizsi.rcom.StreamSourceVnc;
import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.gui.DelegateVideoStreamContainer;
import com.rizsi.rcom.gui.GuiCliArgs;
import com.rizsi.rcom.gui.IVideoStreamContainer;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.commons.UtilEventListener;
import nio.ConnectNio;
import nio.NioThread;
import nio.coolrmi.CoolRMINioClient;
import nio.coolrmi.CoolRMINioRemoter;
import nio.multiplexer.DualChannelProcessorMultiplexer;
import nio.multiplexer.IMultiplexer;

public class Client implements IVideocomCallback {
	public interface IListener
	{

		void connected(Client client);

		void closed(Client client);
		
		void roomEntered(Client client, String room);

		void usersUpdated(List<String> users);

		void updateShares(List<StreamParameters> arrayList);

		void messageReceived(String message);
		
	}
	public void main(String[] args) throws Exception {
		ClientCliArgs a=new ClientCliArgs();
		UtilCli.parse(a, args, true);
		try {
			run(a, new IListener() {
				@Override
				public void connected(Client client) {
				}
				@Override
				public void closed(Client client) {
				}
				@Override
				public void roomEntered(Client client, String room) {
				}
				@Override
				public void usersUpdated(List<String> users) {
				}
				@Override
				public void updateShares(List<StreamParameters> arrayList) {
				}
				@Override
				public void messageReceived(String message) {
					System.out.println("Message: "+message);
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	private boolean isGui;
	public IVideocomConnection conn;
	public int id;
	private Map<String, StreamSink> registered=new HashMap<>();
	private String userName;
	private volatile boolean exit;
	private DelegateVideoStreamContainer selfVideo=new DelegateVideoStreamContainer();
	private AbstractCliArgs args;
	private IMultiplexer multiplexer;
	private volatile Thread mainThread;
	private volatile NioThread nt;
	private volatile List<String> users=new ArrayList<>();
	private String room;
	private IListener l;
	public void run(AbstractCliArgs args, IListener l) throws Exception
	{
		try {
			this.l=l;
			args.apply();
			this.args=args;
			mainThread=Thread.currentThread();
			isGui=args instanceof GuiCliArgs;
			System.out.println("Inited");
			CoolRMINioClient cli=new CoolRMINioClient(Launcher.class.getClassLoader(), false);
			cli.setExecutorService(Executors.newSingleThreadExecutor());
			cli.getServiceRegistry().addProxyType(Client.class, IVideocomCallback.class);
			connect(cli);
			multiplexer=cli.getNioMultiplexer();
			System.out.println("connected");
			IVideocomServer srv=(IVideocomServer) cli.getService(IVideocomServer.class, IVideocomServer.class.getName());
			conn=srv.connect(args.userName==null?""+System.nanoTime():args.userName);
			id=conn.getId();
			userName=conn.getUserName();
			conn.registerCallback(this);
			enterRoom(args.room);
			l.connected(this);
			boolean streamstdin=true;
			if(args instanceof ClientCliArgs)
			{
				ClientCliArgs cargs=(ClientCliArgs) args;
				if(cargs.webcam)
				{
					try {
						// Launch the first camera found
						setVideoStreamingEnabled(args.platform.getCameras(args).values().iterator().next());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				setAudioStreamingEnabled(cargs.audio);
				setVNCShareEnabled(cargs.vnc);
				streamstdin=!cargs.disableStdinMessaging;
			}
			if(streamstdin)
			{
				new Thread("System input"){
					public void run() {
						try(Scanner br=new Scanner(System.in))
						{
							while(!exit&&br.hasNextLine())
							{
								String line=br.nextLine();
								conn.sendMessage(line);
							}
						}
					};
				}.start();
			}
			while(!exit)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cli.close();
		} finally {
			l.closed(this);
		}
	}
	public void close()
	{
		exit=true;
		mainThread.interrupt();
		if(nt!=null)
		{
			nt.close();
			nt=null;
		}
	}
	private void connect(CoolRMINioClient cli) throws Exception {
		nt=new NioThread();
		cli.closedEvent.addListener(new UtilEventListener<CoolRMINioRemoter>() {
			public void eventHappened(CoolRMINioRemoter msg) {
				Client.this.close();
			};
		});
		if(args.connectionString.indexOf('@')>0)
		{
			ProcessBuilder pb=new ProcessBuilder(args.program_ssh, args.connectionString);
			pb.redirectError(Redirect.INHERIT);
			final Process p=pb.start();
			SourceChannel in=ConnectNio.inputStreamToPipe(p.getInputStream());
			in.configureBlocking(false);
			SinkChannel out=ConnectNio.outputStreamToPipe(p.getOutputStream(), new Closeable() {
				
				@Override
				public void close() throws IOException {
					p.destroy();
				}
			});
			out.configureBlocking(false);
			DualChannelProcessorMultiplexer multiplexer=new DualChannelProcessorMultiplexer(nt, in, out, false, 
					VideoConnection.clientIDBS, VideoConnection.serverIDBS);
			cli.connect(multiplexer);
			multiplexer.start();
//			multiplexer.getClosedEvent().addListener(new UtilEventListener<Exception>() {
//				
//				@Override
//				public void eventHappened(Exception msg) {
//				}
//			});
		}else
		{
			int separator=args.connectionString.indexOf(':');
			String host=args.connectionString.substring(0, separator);
			int port=Integer.parseInt(args.connectionString.substring(separator+1));
			cli.connect(nt, new InetSocketAddress(host, port), VideoConnection.clientIDBS, VideoConnection.serverIDBS);
		}
		nt.start();
	}

	@Override
	public void message(String message) {
		l.messageReceived(message);
	}
	@Override
	public void currentShares(List<StreamParameters> arrayList) {
		System.out.println("Current shares: "+arrayList);
		Set<String> keys=new HashSet<>();
		for(StreamParameters p: arrayList)
		{
			if(!registered.containsKey(p.name) && p.sourceClient!=id)
			{
				register(p);
			}
			keys.add(p.name);
		}
		List<String> toUnregister=new ArrayList<>();
		for(String s: registered.keySet())
		{
			if(!keys.contains(s))
			{
				toUnregister.add(s);
			}
		}
		for(String s: toUnregister)
		{
			unregister(s);
		}
		l.updateShares(arrayList);
	}
	@Override
	public void currentUsers(List<String> users) {
		this.users=users;
		l.usersUpdated(users);
	}

	private void register(StreamParameters p) {
		System.out.println("Register: "+p);
		StreamSink sink=p.createSink(this);
		registered.put(p.name, sink);
		try {
			sink.start(args, conn,  multiplexer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void unregister(String s) {
		System.out.println("unregistered: "+s);
		StreamSink c=registered.remove(s);
		c.dispose();
		conn.unregisterStream(s);
	}
	private StreamSourceAudio audio;
	private StreamSourceVideoWebCam video;
	private StreamSourceVnc vnc;
	public void setAudioStreamingEnabled(boolean selected) {
		try {
			if(selected&&audio==null)
			{
				audio=new StreamSourceAudio();
				audio.start(this, userName+"audio");
			}
			if(!selected&&audio!=null)
			{
				audio.close();
				audio=null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setVideoStreamingEnabled(WebCamParameter p) {
		try {
			if(p!=null&&video==null)
			{
				video=new StreamSourceVideoWebCam(p);
				selfVideo.setDelegate(video);
				video.start(this, userName+" cam");
			}
			if(p==null && video!=null)
			{
				video.close();
				video=null;
				selfVideo.setDelegate(null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setVNCShareEnabled(boolean selected) {
		try {
			if(selected&&vnc==null)
			{
				vnc=new StreamSourceVnc();
				vnc.start(this, userName+"screen");
			}
			if(!selected && vnc!=null)
			{
				vnc.close();
				vnc=null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * TODO synchronize object
	 * @return
	 */
	public IVideoStreamContainer getSelfVideo() {
		return selfVideo;
	}
	public List<StreamSink> getStreams()
	{
		synchronized (this) {
			return new ArrayList<>(registered.values());
		}
	}

	public boolean isGUI() {
		return isGui;
	}

	public AbstractRcomArgs getArgs() {
		return args;
	}
	public IMultiplexer getMultiplexer() {
		return multiplexer;
	}

	public void addListener(int backChannel, StreamSourceVnc streamSourceVnc) {
		// TODO Auto-generated method stub
		
	}

	public ChannelOutputStream createStream() {
		// TODO Auto-generated method stub
		return null;
	}
	public void enterRoom(String room) {
		if(room!=null&&room.length()>0)
		{
			setAudioStreamingEnabled(false);
			setVideoStreamingEnabled(null);
			setVNCShareEnabled(false);
			String s=conn.enterRoom(room);
			l.roomEntered(this, s);
		}else
		{
			conn.leaveRoom();
			l.roomEntered(this, null);
		}
	}
	public void sendMessage(String text) {
		conn.sendMessage(text);
	}
}
