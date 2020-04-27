/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <pre>
 * 随机数工具类
 * ThreadLocalRandom性能提高一倍，且线程安全
 * </pre>
 * 
 * @author reison
 * @time 2017年3月24日 上午10:22:45
 */
public final class RandomUtil {

	/**
	 * <pre>
	 * 获取随机数
	 * 包含min，不包含max
	 * </pre>
	 *
	 * @param min 包含
	 * @param max 不包含
	 * @return
	 */
	public final static int rand(int min, int max) {
		if (min >= max) {
			return max;
		}
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	/**
	 * <pre>
	 * 0->max随机数
	 * 1000W次30毫秒
	 * </pre>
	 *
	 * @param max 不包含
	 * @return
	 */
	public final static int rand(int max) {
		return rand(0, max);
	}

	/**
	 * <pre>
	 * 1->100(包含)随机数
	 * </pre>
	 *
	 * @return
	 */
	public final static int rand100() {
		return rand(1, 101);
	}

	/**
	 * <pre>
	 * 1->1000(包含)随机数
	 * </pre>
	 *
	 * @return
	 */
	public final static int rand1000() {
		return rand(1, 1001);
	}

	/**
	 * <pre>
	 * 1->10000(包含)随机数
	 * </pre>
	 *
	 * @return
	 */
	public final static int rand10000() {
		return rand(1, 10001);
	}

	/**
	 * <pre>
	 * 生成一定范围的不重复随机数组
	 * 100万次，10位，210ms左右
	 * </pre>
	 *
	 * @param min 包含
	 * @param max 不包含
	 * @return
	 */
	public static int[] randomNoSameArray(int min, int max) {
		return randomNoSameArray(min, max, 0);
	}

	/**
	 * <pre>
	 * 生成一定范围的不重复随机数组
	 * 100万次，10位，210ms左右
	 * </pre>
	 * 
	 * @param min 包含
	 * @param max 不包含
	 * @param count 数组长度(<=max-min)
	 * @return
	 */
	public static int[] randomNoSameArray(int min, int max, int count) {
		if (min >= max) {
			return new int[] { min };
		}
		if (min < 0 || max < 0) {
			return new int[] { 0 };
		}
		if (min == max - 1) {
			return new int[] { min };
		}
		if (count == 0) {
			count = max - min;
		}
		// 最大长度
		if (count > max - min) {
			count = max - min;
		}
		int[] rsc = new int[max];
		for (int i = min; i < max; i++) {
			rsc[i - min] = i;// 填充
		}
		int ran = 0;
		int temp = 0;
		int endIndex = 0;
		int[] result = new int[max];
		for (int i = 0; i < count; i++) {
			endIndex = count - i - 1;
			if (endIndex > 0) {
				ran = ThreadLocalRandom.current().nextInt(endIndex);
			} else {
				ran = 0;
			}
			temp = rsc[ran];// 生成
			result[i] = temp;
			rsc[ran] = rsc[endIndex];// 交换
			rsc[endIndex] = temp;
		}
		return result;
	}

	private static char ch[] = { '3', '4', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 't', 'u', 'v', 'w', 'x', 'y', '3', '4', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G' };// 最后又重复，因为需要凑足数组长度为64

	private static Random random = new Random();

	// 生成指定长度的随机字符串
	public static synchronized String createRandomString(int length) {
		if (length > 0) {
			int index = 0;
			char[] temp = new char[length];
			int num = random.nextInt();
			for (int i = 0; i < length % 5; i++) {
				temp[index++] = ch[num & 63];// 取后面六位，记得对应的二进制是以补码形式存在的。
				num >>= 6;// 63的二进制为:111111
				// 为什么要右移6位？因为数组里面一共有64个有效字符。为什么要除5取余？因为一个int型要用4个字节表示，也就是32位。
			}
			for (int i = 0; i < length / 5; i++) {
				num = random.nextInt();
				for (int j = 0; j < 5; j++) {
					temp[index++] = ch[num & 63];
					num >>= 6;
				}
			}
			return new String(temp, 0, length);
		} else if (length == 0) {
			return "";
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * <pre>
	 * 激活码
	 * </pre>
	 */
	public final static void arrangeCode() {
		BufferedWriter writer = null;
		PrintWriter printWriter = null;

		try {
			// 1、创建cdkey目录(不存在的情况下)
			String filePath = "D:\\cdkey\\19.txt";
			File cdkeyFile = new File(filePath);
			if (!cdkeyFile.exists()) {
				cdkeyFile.createNewFile();
			}

			// 2、生成激活码文件
			printWriter = new PrintWriter("D:\\cdkey\\19.txt");
			writer = new BufferedWriter(printWriter);
			for (int i = 0; i < 500000; i++) {// 生成指定个数的字符串
				StringBuilder sb = new StringBuilder();
				sb.append(createRandomString(10));
				sb.append("17");
				sb.append(createRandomString(7));
				writer.write(sb.toString());
				if (File.separator.equals("\\")) {
					writer.write("\n");
				} else if (File.separator.equals("/")) {
					writer.write("\r\n");
				}
			}

			writer.flush();

		} catch (FileNotFoundException e) {
			Log.error("生活激活码 FileNotFoundException:", e);
		} catch (IOException e) {
			Log.error("生活激活码 IOException:{}", e);
		} finally {
			if (null != printWriter) {
				printWriter.close();
			}

			try {
				if (null != writer) {
					writer.close();
				}

			} catch (IOException e) {
				Log.error("生活激活码关闭流 IOException:", e);
			}
		}

	}

	/**
	 * <pre>
	 * 根据配置的几率map，随机出来指定数量随机数据
	 *  概率为value，返回num个不重复的key集合
	 * </pre>
	 * 
	 * @param <K>
	 * @param probs <obj,prob>
	 * @param num 随机的次数，最多获取
	 * @return
	 */
	public static <K> List<K> randomListByProb(Map<? extends K, Integer> probs, int num) {
		List<K> results = new ArrayList<>();
		if (probs.size() >= 0) {
			try {
				List<K> keyList = new ArrayList<>();
				List<Integer> probList = new ArrayList<>();
				for (Entry<? extends K, Integer> entry : probs.entrySet()) {
					K key = entry.getKey();
					keyList.add(key);
					probList.add((Integer) entry.getValue() < 0 ? 0 : entry.getValue());
				}
				while (!probList.isEmpty() && num-- > 0) {
					int index = randomIndexByProb(probList);
					if (index >= 0) {
						results.add(keyList.get(index));
					}
				}
			} catch (Exception e) {
				Log.error("计算机率错误" + probs.toString(), e);
			}
		}
		if (results.size() <= 0) {
			results.clear();
		}
		return results;
	}

	/**
	 * <pre>
	 * 根据配置的概率集合返回序号
	 * probs = [10,15,50,20,13]
	 * 转换后
	 * newprobs = [10,25,75,95,108]
	 * </pre>
	 * 
	 * @param probs 根据总机率返回序号
	 * @return
	 */
	public static int randomIndexByProb(List<Integer> probs) {
		try {
			LinkedList<Integer> newprobs = new LinkedList<Integer>();
			// [0,0,0,0,0,0,0,0,10000]
			for (int i = 0; i < probs.size(); i++) {
				// if (probs.get(i) > 0) {
				if (i == 0) {
					newprobs.add(probs.get(i));
				} else {
					newprobs.add(newprobs.get(i - 1) + probs.get(i));
				}
				// }
			}
			if (newprobs.size() <= 0) {
				return -1;
			}
			int last = newprobs.getLast();
			if (last == 0) {
				return -1;
			}
			int random = rand(last);
			for (int i = 0; i < newprobs.size(); i++) {
				int value = newprobs.get(i);
				if (value > random) {
					return i;
				}
			}
		} catch (Exception e) {
			Log.error("计算机率错误" + probs.toString(), e);
		}
		return -1;
	}

	/**
	 * <pre>
	 * 随机取list里面的一个元素
	 * </pre>
	 *
	 * @param list
	 * @return
	 */
	public static <V> V randomGetIndex(List<V> list) {
		if (list != null && list.size() > 0) {
			int size = list.size();
			int rand = rand(size);
			V v = list.get(rand);
			return v;
		}
		return null;

	}

	public static void main(String[] args) {
		System.out.println(RandomStrUtil.randomStr(8));
	}

}
