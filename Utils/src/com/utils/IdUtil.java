/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <pre>
 * ID工具
 * </pre>
 * 
 * @author jiahao.fang
 * @time 2017年8月14日 下午2:27:26
 */
public class IdUtil {

	private static final Random random = new Random();

	/**
	 * <pre>
	 * 不重复率：
	 * 999999/1000000     
	 * 9999987/10000000   
	 * 10000000个数据的生成时间8s
	 * ！建议使用
	 * </pre>
	 *
	 * @return
	 */
	public static long createOnlyLongId() {
		long t1 = 0x7FFFFFFF & System.currentTimeMillis();
		return t1 << 32 | Math.abs(random.nextInt());
	}

	/**
	 * <pre>
	 * 不重复率：
	 * 999350/1000000
	 * 9988421/10000000
	 * 10000000个数据的生成时间19s
	 * ！不建议使用
	 * </pre>
	 *
	 * @return
	 */
	private static long createID2() {
		String a = System.currentTimeMillis() + "";
		String b = random.nextInt(1000000) + "";
		long c = Long.parseLong(a + b);
		return c;
	}

	public static void main(String[] args) {
		List<Long> list = new ArrayList<>();
		Set<Long> set = new HashSet<>();
		List<Long> list2 = new ArrayList<>();
		Set<Long> set2 = new HashSet<>();
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			long a = createOnlyLongId();
			list.add(a);
			set.add(a);
		}
		long time2 = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			long b = createID2();
			list2.add(b);
			set2.add(b);
		}
		long time3 = System.currentTimeMillis();
		System.out.println(set.size() + "/" + list.size() + "时间：  " + (time2 - time1) / 1000 + "s");
		System.out.println(set2.size() + "/" + list2.size() + "时间：  " + (time3 - time2) / 1000 + "s");
		System.out.println(createOnlyLongId());
		System.out.println(createID2());
	}
}
