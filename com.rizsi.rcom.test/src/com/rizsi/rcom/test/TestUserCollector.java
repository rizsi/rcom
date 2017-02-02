package com.rizsi.rcom.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.rizsi.rcom.ssh.UserCollectorTemplate;
import com.rizsi.rcom.ssh.UserKey;

import hu.qgears.commons.UtilFile;

public class TestUserCollector {
	@Test
	public void test() throws IOException
	{
		List<UserKey> keys=new ArrayList<UserKey>();
		keys.add(new UserKey("test-user", "kulcsa"));
		keys.add(new UserKey("test-user2", "masik kulcs"));
		String s=new UserCollectorTemplate().generate(keys, "example.com", 9643);
		Assert.assertEquals(UtilFile.loadAsString(getClass().getResource("authorized_keys.txt")), s);
	}
}
