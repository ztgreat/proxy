package com.proxy.common.test.util;

import com.proxy.common.util.SessionIDGenerate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SessionNumberGenerateTest {
	@Test
	public void testAdd() {
		List<Thread> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list.add(new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(SessionIDGenerate.getInstance().generateId());
				}
			}));
			
		}
		for (Thread t : list) {
			t.start();
		}
	}
}
