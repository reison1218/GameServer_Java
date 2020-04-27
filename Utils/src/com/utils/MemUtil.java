/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;

/**
 * <pre>
 * </pre>
 * 
 * @author reison
 * @time 2017年3月21日 下午10:20:33
 */
public final class MemUtil {

	public final static void showMem() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		System.out.println(memoryMXBean.getHeapMemoryUsage());
		System.out.println(memoryMXBean.getNonHeapMemoryUsage());
	}

	public final static void showDirectMem() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Class<?> c = Class.forName("java.nio.Bits");
					Field field1 = c.getDeclaredField("maxMemory");
					field1.setAccessible(true);
					Field field2 = c.getDeclaredField("reservedMemory");
					field2.setAccessible(true);
					while (true) {
						synchronized (c) {
							Long max = (Long) field1.get(null);
							Long reserve = (Long) field2.get(null);
							System.out.println(reserve + "B/" + max + "B");
						}
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public static void main(String[] args) {
		showMem();
		showDirectMem();
	}

}
