/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

/**
 * <pre>
 * 二进制运算工具包
 * 注意!!!
 * 只支持最大32位运算
 * position参数不允许小于1或者大于32
 * 使用到该方法工具类的方法都要在方法外判断该参数是否为异常参数
 * </pre>
 * 
 * @author reison
 * @time 2017年4月20日 下午5:55:46
 */
public class BinaryUtil {

	/**
	 * <pre>
	 * 检测二进制的某一位是否为1
	 * 位置从右往左开始
	 * 如 00001101
	 *   第一位是1
	 *   第二位是0
	 *   第三位是1
	 *   ...
	 * </pre>
	 *
	 * @param binary 二进制
	 * @param position 二进制位,从右往左的位数
	 * @return
	 */
	public static boolean checkBinPosIs1(int binary, int position) {
		if (!argsCheck(position)) {
			return false;
		}
		return getBinPosValue(binary, position) == 1;
	}

	/**
	 * <pre>
	 * 选择性的将二进制的某位设置为1
	 * 位置从右往左开始
	 * 如  binary 参数为00000000
	 * 	 position参数为1,则结果为00000001
	 * 	 position参数为2,则结果为00000010
	 * 	 position参数为3,则结果为00000100
	 * </pre>
	 *
	 * @param binary 二进制
	 * @param position 二进制位,从右往左的位数
	 * @return
	 */
	public static int setBinPos1(int binary, int position) {
		if (!argsCheck(position)) {
			return -1;
		}
		return binary | (1 << (position - 1));
	}

	/**
	 * <pre>
	 * 选择性的将二进制的某位设置为0
	 * 位置从右往左开始
	 * 如  binary 参数为11111111
	 * 	 position参数为1,则结果为11111110
	 * 	 position参数为2,则结果为11111101
	 * 	 position参数为3,则结果为11111011
	 * </pre>
	 *
	 * @param binary 二进制
	 * @param position 二进制位,从右往左的位数
	 * @return
	 */
	public static int setBinPos0(int binary, int position) {
		if (!argsCheck(position)) {
			return -1;
		}
		return binary & ~(1 << (position - 1));
	}

	/**
	 * <pre>
	 * 检测二进制的某一位的值
	 * 位置从右往左开始
	 * 如 00001101
	 *   第一位是1
	 *   第二位是0
	 *   第三位是1
	 *   ...
	 * </pre>
	 *
	 * @param binary 二进制
	 * @param position 二进制位,从右往左的位数
	 * @return
	 */
	public static int getBinPosValue(int binary, int position) {
		if (!argsCheck(position)) {
			return -1;
		}
		return (binary >>> (position - 1)) & 1;
	}

	/**
	 * <pre>
	 * 参数检测
	 * </pre>
	 *
	 * @param position
	 */
	public static boolean argsCheck(int position) {
		if (position > 32 || position < 1) {
			Log.error("二进制验证position参数非法, position:" + position, new Exception());
			return false;
		}
		return true;
	}

	/**
	 * <pre>
	 * 参数n转换为二进制之后1的个数
	 * </pre>
	 *
	 * @param position
	 */
	public static int getNumberOf1(int n) {
		int count = 0;
		while (n != 0) {
			n = n & (n - 1);
			count++;
		}
		return count;
	}

	public static void main(String[] args) {

		System.out.println(getNumberOf1(5));
		// int position = 7;
		// int binary = 321;
		// System.out.println(Integer.toBinaryString(binary));
		// System.out.println(getBinaryPositionValue(binary, position));
		// binary >>>= position;
		// binary = getBinaryPositionValue(binary, position);
		// System.out.println(Integer.toBinaryString(binary));
		// binary = setBinaryPosition1(binary, position);
		// System.out.println(Integer.toBinaryString(binary));

		// binary = setBinaryPosition1(binary, position);
		// System.out.println(Integer.toBinaryString(binary));
		//
		// binary = 0x40;
		// System.out.println(Integer.toBinaryString(binary));
		// binary = setBinaryPosition1(binary, 15);
		// System.out.println(Integer.toBinaryString(binary));
		//
		// binary = binary ^ (1 << (8 - 1));
		// System.out.println(Integer.toBinaryString(binary));
		// System.out.println(binary);

		// System.out.println(getPositionValue(5, 1));
		// int i100 = 1;
		// int i200 = 1;
		// System.out.println(Integer.toBinaryString(i100));
		// System.out.println(Integer.toBinaryString(i200));
		// System.out.println("^ " + Integer.toBinaryString(i200 ^ i100));
		// System.out.println("& " + Integer.toBinaryString(i200 & i100));
		// System.out.println("| " + Integer.toBinaryString(i200 | i100));
	}
}
