/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 二进制运算工具包
 * </pre>
 * 
 * @author reison
 * @time 2017年4月20日 下午5:55:46
 */
public class StringUtils {

	/**
	 * <pre>
	 * byte 转换成 字符串
	 * </pre>
	 *
	 * @param tempByte
	 * @return
	 */
	public static String byteToStr(byte[] tempByte) {
		try {
			String res = new String(tempByte, "ISO8859-1");
			return res;
		} catch (UnsupportedEncodingException e) {
			Log.error("str :" + Arrays.toString(tempByte), e);
			return "";
		}
	}

	/**
	 * <pre>
	 * byte 转换成 字符串 （祛除byte末尾空数组）
	 * </pre>
	 *
	 * @param tempByte
	 * @return
	 */
	public static String byteToStrTrim(byte[] tempByte) {
		try {
			int length = 0;
			for (int i = tempByte.length - 1; i > 0; i--) {
				if (tempByte[i] > 0) {
					length = i + 1;
					break;
				}
			}
			byte[] res = new byte[length];
			System.arraycopy(tempByte, 0, res, 0, length);
			return new String(res, "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			Log.error("str :" + Arrays.toString(tempByte), e);
			return "";
		}
	}

	/**
	 * <pre>
	 * byte 转换成 字符串
	 * </pre>
	 *
	 * @param tempByte
	 * @return
	 */
	public static byte[] strToByte(String str) {
		try {
			byte[] questSite = str.getBytes("ISO8859-1");
			return questSite;
		} catch (UnsupportedEncodingException e) {
			Log.error("str :" + str, e);
			return new byte[0];
		}

	}

	public static String getKey(int p1, int p2) {
		return p1 + "_" + p2;
	}

	/**
	 * <pre>
	 * 正则匹配第一个结果
	 * </pre>
	 *
	 * @param pattern
	 * @param str
	 * @return
	 */
	public final static String getMatch0(String pattern, String str) {
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find()) {
			return m.group(0);
		}
		return "";
	}

	/**
	 * <pre>
	 * 检测是否为数字
	 * </pre>
	 *
	 * @param checkStr
	 * @return
	 */
	public static boolean checkIsNum(String checkStr) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher("115");
		return isNum.matches();
	}

	public final static String format1(String preStr, Object... strs) {
		return String.format(preStr, strs);
	}

	public final static String format(String preStr, Object... strs) {
		return MessageFormat.format(preStr, strs);
	}

	public final static String format3(String preStr, Object... strs) {
		for (int i = 0, len = strs.length; i < len; i++) {
			preStr = preStr.replaceFirst("__", strs[i].toString());
		}
		return preStr;
	}

	/**
	 * <pre>
	 * 字符串格式化评测
	 * abcabc哈哈哈12312313JKJIU什么鬼kjk哈,String.format耗时：257ms
	 * abcabc哈哈哈12312313JKJIU什么鬼kjk哈,MessageFormat.format耗时：137ms
	 * abcabc哈哈哈12312313JKJIU什么鬼kjk哈,String.replace耗时：183ms
	 * </pre>
	 */
	public final static void benchMarkStrFormat() {
		final String str1 = "abc%s哈哈哈%s12313JKJIU%skjk哈";
		final String str2 = "abc{0}哈哈哈{1}12313JKJIU{2}kjk哈";
		final String str3 = "abc__哈哈哈__12313JKJIU__kjk哈";
		Object[] strs = new Object[] { "abc", "123", "什么鬼" };
		long st = System.currentTimeMillis();
		String str = "";
		for (int i = 0; i < 100000; i++) {
			str = format1(str1, strs);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println(str + ",String.format耗时：" + ct + "ms");

		st = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			str = format(str2, strs);
		}
		ct = System.currentTimeMillis() - st;
		System.out.println(str + ",MessageFormat.format耗时：" + ct + "ms");

		st = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			str = format3(str3, strs);
		}
		ct = System.currentTimeMillis() - st;
		System.out.println(str + ",String.replace耗时：" + ct + "ms");
	}

	public final static void testFormat() {
		final String str = "{0};{1};{2};{3};{4};{5};{6};{7};{8}";
		String nStr = format(str, TimeUtil.getDateFormat(), "40025", "aiweiyou", "5683b0b9148c068951", "14.108.148.38", "190175", "黛绮丝灬", "CHA");
		System.out.println(nStr);
	}

	/**
	 * <pre>
	 * 字符串某字母转大写
	 * </pre>
	 *
	 * @param string
	 * @param index
	 * @return
	 */
	public final static String toUpperCaseByIndex(String string, int index) {
		char[] chArr = string.toCharArray();
		chArr[index] = toUpperCase(chArr[index]);
		return String.valueOf(chArr);
	}

	/**
	 * <pre>
	 * 字符串某字母转小写
	 * </pre>
	 *
	 * @param string
	 * @param index
	 * @return
	 */
	public final static String toLowerCaseByIndex(String string, int index) {
		if (isEmpty(string)) {
			return string;
		}
		char[] chArr = string.toCharArray();
		chArr[index] = toLowerCase(chArr[index]);
		return String.valueOf(chArr);
	}

	/**
	 * <pre>
	 * 字符转成大写
	 * </pre>
	 *
	 * @param chat
	 * @return
	 */
	public final static char toUpperCase(char chat) {
		if (97 <= chat && chat <= 122) {
			chat ^= 32;
		}
		return chat;
	}

	/**
	 * <pre>
	 * 字符转成小写
	 * </pre>
	 *
	 * @param chat
	 * @return
	 */
	public final static char toLowerCase(char chat) {
		if (65 <= chat && chat <= 90) {
			chat |= 32;
		}
		return chat;
	}

	/**
	 * <pre>
	 * 执行结果:ToUpperCase,执行次数:10000000,耗时:186ms
	 * </pre>
	 */
	public final static void testStrUpperCase() {
		String newStr = "";
		final int count = 10000000;
		final String str = "ToUpperCase";
		final long st = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			newStr = toLowerCaseByIndex(str, 0);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("执行结果:" + newStr + ",执行次数:" + count + ",耗时:" + ct + "ms");
	}

	/**
	 * <pre>
	 * 字符串空判断
	 * </pre>
	 *
	 * @param str
	 * @return
	 */
	public final static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	/**
	 * <pre>
	 * 左填充字符
	 * </pre>
	 *
	 * @param preMsg
	 * @param pad
	 * @param len
	 * @return
	 */
	public final static String leftPad(String preMsg, char pad, Integer len) {
		StringBuilder sb = new StringBuilder(preMsg);
		while (sb.length() < len) {
			sb.insert(0, pad);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		testStrUpperCase();
	}
}
