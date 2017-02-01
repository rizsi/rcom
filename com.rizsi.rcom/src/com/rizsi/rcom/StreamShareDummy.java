package com.rizsi.rcom;

public class StreamShareDummy extends StreamShare {

	public StreamShareDummy(VideoConnection videoConnection, StreamParameters params) {
		super(videoConnection, params);
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
