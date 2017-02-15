package com.rizsi.rcom.test.resample;

import org.junit.Test;

import com.rizsi.rcom.AbstractRcomArgs;
import com.rizsi.rcom.audio.JitterResampler;
import com.rizsi.rcom.test.TestArguments;

public class TestJitterResampler {
	@Test
	public void test() throws Exception
	{
		AbstractRcomArgs a=TestArguments.getArgs();
		try(JitterResampler resampler=new JitterResampler(a, 8000, 256, 2))
		{
			byte[] data=new byte[512];
			for(int i=0;i<10000;++i)
			{
				resampler.writeInput(data);
				resampler.readOutput(data);
				if(i%100==0)
				{
					resampler.writeInput(data);
				}
			}
			for(int i=0;i<20000;++i)
			{
				resampler.writeInput(data);
				resampler.readOutput(data);
				if(i%100==0)
				{
					resampler.readOutput(data);
				}
			}
		}
	}
}
