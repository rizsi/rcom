package com.rizsi.rcom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamShareDummy extends StreamShare {

	public StreamShareDummy(VideoConnection videoConnection, StreamParameters params) {
		super(videoConnection, params);
	}
	byte buffer[]=new byte[1024];
	OutputStream os=new NullOutputStream();
	@Override
	public void readFully(InputStream is, int len) throws IOException {
		IChannelReader.pipeToFully(is, len, buffer, os);
	}

	@Override
	public StreamRegistration registerClient(VideoConnection videoConnection, int channel) {
		return new StreamRegistration() {
			
			@Override
			public void launch() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public IStreamData getData() {
				return new StreamDataDummy();
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public IStreamData getStreamData() {
		// TODO Auto-generated method stub
		return null;
	}

}
