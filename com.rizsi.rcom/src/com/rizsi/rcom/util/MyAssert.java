package com.rizsi.rcom.util;

public class MyAssert {

	public static void myAssert(boolean b) {
		if(!b)
		{
			throw new RuntimeException();
		}
	}

}
