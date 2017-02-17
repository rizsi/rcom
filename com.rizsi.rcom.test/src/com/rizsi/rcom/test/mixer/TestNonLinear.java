package com.rizsi.rcom.test.mixer;

import org.junit.Assert;
import org.junit.Test;

import com.rizsi.rcom.audio.MixingOutput;

public class TestNonLinear {
	@Test
	public void test01()
	{
		testValue(MixingOutput.linearLimit, MixingOutput.linearLimit);
		testValue(MixingOutput.linearLimit+1, MixingOutput.linearLimit+1);
		testValue(MixingOutput.linearLimit+2, MixingOutput.linearLimit+1);
		testValue(MixingOutput.linearLimit+3, MixingOutput.linearLimit+2);
		testValue(MixingOutput.linearLimit+4, MixingOutput.linearLimit+3);
		testValue(MixingOutput.linearLimit+5, MixingOutput.linearLimit+4);
		testValue(MixingOutput.linearLimit+6, MixingOutput.linearLimit+5);
		testValue(MixingOutput.linearLimit+7, MixingOutput.linearLimit+6);
		testValue(Short.MAX_VALUE, 28643);
		testValue(Short.MAX_VALUE*2, 29048);
		testValue(Short.MAX_VALUE*10, 29463);
		testValue(Short.MAX_VALUE*50, 29799);
		testValue(Short.MAX_VALUE*100, 29940);

		testValue(MixingOutput.negLinearLimit, MixingOutput.negLinearLimit);
		testValue(MixingOutput.negLinearLimit-1, MixingOutput.negLinearLimit-1);
		testValue(MixingOutput.negLinearLimit-2, MixingOutput.negLinearLimit-1);
		testValue(MixingOutput.negLinearLimit-3, MixingOutput.negLinearLimit-2);
		testValue(MixingOutput.negLinearLimit-4, MixingOutput.negLinearLimit-3);
		testValue(MixingOutput.negLinearLimit-5, MixingOutput.negLinearLimit-4);
		testValue(MixingOutput.negLinearLimit-6, MixingOutput.negLinearLimit-5);
		testValue(MixingOutput.negLinearLimit-7, MixingOutput.negLinearLimit-6);
		testValue(Short.MIN_VALUE, -28643);
		testValue(Short.MIN_VALUE*2, -29049);
		testValue(Short.MIN_VALUE*10, -29463);
		testValue(Short.MIN_VALUE*50, -29799);
		testValue(Short.MIN_VALUE*100, -29940);

	}
	private void testValue(int a, int b)
	{
		int c=MixingOutput.nonLinearResampling(a);
		Assert.assertEquals("Nonlinear transform: "+a, b, c);
	}
}
