package com.rizsi.rcom.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.IVideocomCallback;
import com.rizsi.rcom.IVideocomConnection;
import com.rizsi.rcom.IVideocomServer;
import com.rizsi.rcom.StreamParameters;
import com.rizsi.rcom.StreamSink;
import com.rizsi.rcom.StreamSourceAudio;
import com.rizsi.rcom.StreamSourceVideoWebCam;
import com.rizsi.rcom.StreamSourceVnc;
import com.rizsi.rcom.VideoClientConnectionFactory;
import com.rizsi.rcom.gui.DelegateVideoStreamContainer;
import com.rizsi.rcom.gui.GuiCliArgs;
import com.rizsi.rcom.gui.IVideoStreamContainer;
import com.rizsi.rcom.webcam.WebCamParameter;

import hu.qgears.coolrmi.CoolRMIClient;

public class Client implements IVideocomCallback {
	public void main(String[] args) throws Exception {
		ClientCliArgs a=new ClientCliArgs();
		UtilCli.parse(a, args, true);
		run(a);
	}
	private boolean isGui;
	public IVideocomConnection conn;
	public int id;
	private Map<String, StreamSink> registered=new HashMap<>();
	public VideoClientConnectionFactory fact;
	private String userName;
	private volatile boolean exit;
	private DelegateVideoStreamContainer selfVideo=new DelegateVideoStreamContainer();
	private AbstractCliArgs args;
	public void run(AbstractCliArgs args) throws IOException
	{
		this.args=args;
		isGui=args instanceof GuiCliArgs;
		if(!args.disablePulseEchoCancellation)
		{
			// TODO does setting property this way have an effect on pulseaudio?
			System.setProperty("PULSE_PROP", "filter.want=echo-cancel");
		}
		System.out.println("Inited");
		fact=new VideoClientConnectionFactory(args);
		CoolRMIClient cli=new CoolRMIClient(Launcher.class.getClassLoader(), fact, false);
		cli.getServiceRegistry().addProxyType(Client.class, IVideocomCallback.class);
		System.out.println("connected");
		IVideocomServer srv=(IVideocomServer) cli.getService(IVideocomServer.class, IVideocomServer.id);
		userName=""+System.nanoTime();
		conn=srv.connect(userName);
		id=conn.getId();
		conn.registerCallback(this);
		boolean streamstdin=false;
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
			try(Scanner br=new Scanner(System.in))
			{
				while(br.hasNextLine())
				{
					String line=br.nextLine();
					conn.sendMessage(line);
				}
			}
		}else
		{
			while(!exit)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		cli.close();
	}

	@Override
	public void message(String message) {
		System.out.println("Message: "+message);
	}
	@Override
	public void currentShares(List<StreamParameters> arrayList) {
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
	}

	private void register(StreamParameters p) {
		System.out.println("Register: "+p);
		StreamSink sink=p.createSink(this);
		registered.put(p.name, sink);
		try {
			sink.start(args, conn,  fact.getMultiplexer());
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
}
