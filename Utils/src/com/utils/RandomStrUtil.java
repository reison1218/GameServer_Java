/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <pre>
 * 随机字符串工具
 * ThreadLocalRandom性能提高一倍，且线程安全
 * </pre>
 * 
 * @author reison
 * @time 2016-6-2 下午4:10:16
 */
public final class RandomStrUtil {

	private final static char[] chs1 = "1234567890".toCharArray();

	private final static char[] chs2 = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();

	private final static char[] chs3 = "1234567890qwertyuiopasdfghjklzxcvbnm".toCharArray();

	public static <T> void main(String[] args) throws Exception {
		// testRandomStr();
		// testRandomStrTime();
		System.out.println(randomStr(8));
		System.out.println(randomStr(16));
		// System.out.println(randomStr(32));
		// testRandomId();
	}

	public static void testRandomStr() {
		int size = 10000000;
		HashSet<String> set = new HashSet<String>(size);
		for (int i = 0; i < size; i++) {
			set.add(randomStr(12));
		}
		System.out.println(size - set.size());
	}

	public static void testRandomStrTime() {
		int size = 10000000;
		long st = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			randomStr(12);
		}
		long ed = System.currentTimeMillis();
		System.out.println(ed - st);
	}

	/**
	 * <pre>
	 * 返回一串指定长度的，由数字、大小写字母组成的随机串
	 * ！长度1-100，否则返回空字符串
	 * 100w次长度6，80ms左右
	 * 100w次长度8，90ms左右
	 * 1000w次长度8，620ms左右
	 * 100w次长度6，999990个不重复，10左右重复
	 * 100w次长度8，基本0重复
	 * 1000w次长度6，9999100个不重复，900左右重复
	 * 1000w次长度8，<5重复
	 * 1000w次长度10，无重复
	 * 做精度要求不高的唯一串,8长度即可
	 * </pre>
	 * 
	 * @param len
	 * @return
	 */
	public static String randomStr(int len) {
		if (len < 1) {
			return "";
		}
		if (len > 100) {
			len = 100;
		}
		final int total = chs2.length;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chs2[ThreadLocalRandom.current().nextInt(total)]);
		}
		return sb.toString();
	}

	public static String randomLowerStr(int len) {
		if (len < 1) {
			return "";
		}
		if (len > 100) {
			len = 100;
		}
		final int total = chs3.length;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chs3[ThreadLocalRandom.current().nextInt(total)]);
		}
		return sb.toString();
	}

	/**
	 * <pre>
	 * 返回一串指定长度由数字组成的随机串
	 * ！长度1-100，否则返回空字符串
	 * 100w次长度6，150ms左右
	 * 重复率较高，不能做唯一串
	 * </pre>
	 * 
	 * @param len
	 * @return
	 */
	public static String randomNum(int len) {
		if (len < 1) {
			return "";
		}
		if (len > 100) {
			len = 100;
		}
		final int total = chs1.length;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chs1[ThreadLocalRandom.current().nextInt(total)]);
		}
		return sb.toString();
	}

	/**
	 * <pre>
	 * 生成不重复整型Id
	 * </pre>
	 *
	 * @return
	 */
	public final static int randomNumId() {
		return Integer.parseInt(("1" + randomNum(6) + (System.currentTimeMillis() / 1000 + "").substring(7)));
	}

}
