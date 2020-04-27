/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.parser.JSONScanner;

/**
 * <pre>
 * 计算
 * </pre>
 */
public class MathUtil {

	/**
	 * <pre>
	 * 计算最大公约数
	 * </pre>
	 * 
	 * @param M
	 * @param N
	 * @return
	 */
	public static int commonDivisor(int M, int N) {
		if (N < 0 || M < 0) {
			Log.error("Common divisor calc error,M:" + M + ",N:" + N);
			return -1;
		}
		if (N == 0) {
			return M;
		}
		return commonDivisor(N, M % N);
	}

	/**
	 * <pre>
	 * 螺旋遍历（从内到外）
	 * </pre>
	 * 
	 * @param index 第N个点
	 * @return 以(0,0)为原点的第N个点的坐标
	 */
	public static Pair calcScrewPoint(int index) {
		if (index == 1) {
			return new Pair(0, 0);
		}
		int inMaxSqrt = (int) Math.sqrt(index - 1);// 找内圈最大值——一个基数的平方
		if (inMaxSqrt % 2 == 0) {// 向下取基数整
			inMaxSqrt--;
		}
		int d = inMaxSqrt + 1;// 所在正方形的边长，必定为偶数
		int r = d / 2;// 半径
		int shift = index - (int) Math.pow(inMaxSqrt, 2) - 1;// 当前索引在其所处正方形上的偏移量，减一为运算方便
		int side = shift / d;
		int x = -r;
		int y = -r;
		switch (side) {
		case 0:
			x += d;
			y += shift + 1;
			break;
		case 1:
			x += d - (shift - d) - 1;
			y += d;
			break;
		case 2:
			y += d - (shift - d * 2) - 1;
			break;
		case 3:
			x += 1 + shift - d * 3;
			break;
		}
		Pair point = new Pair(x, y);
		return point;
	}

	/**
	 * <pre>
	 * 是否在数组中
	 * </pre>
	 * 
	 * @param array
	 * @param num
	 * @return
	 */
	public static boolean inArray(int[] array, int num) {
		if (array == null || array.length < 1) {
			return false;
		}
		for (int i = 0, len = array.length; i < len; i++) {
			if (num == array[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 是否在数组中
	 * </pre>
	 * 
	 * @param array
	 * @param num
	 * @return
	 */
	public static boolean inArray(short[] array, int num) {
		if (array == null || array.length < 1) {
			return false;
		}
		for (int i = 0, len = array.length; i < len; i++) {
			if (num == array[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 是否在集合中
	 * </pre>
	 *
	 * @param list
	 * @param obj
	 * @return
	 */
	public final static boolean inNumList(List<Number> list, Number obj) {
		for (Number o : list) {
			if (o.longValue() == obj.longValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 向下计算容错，比如采集，捕捉，冥想的秒数，防止客户端采集完成，服务端尚未完成的情况
	 * </pre>
	 * 
	 * @param preValue
	 * @return
	 */
	public static int calcDownTolerance(int preValue) {
		return (int) (preValue * 0.8f);
	}

	/**
	 * <pre>
	 * 向上计算容错
	 * </pre>
	 * 
	 * @param preValue
	 * @return
	 */
	public static int calcUpTolerance(int preValue) {
		return (int) (preValue * 1.2f);
	}

	public static int sumArray(int[] attr) {
		if (attr == null || attr.length == 0) {
			return 0;
		}
		return sumArray(attr, 0, attr.length);
	}

	/**
	 * <pre>
	 * 计算数组和
	 * </pre>
	 *
	 * @param attr     int数组
	 * @param startIdx 起始索引(0-[len-1])-包含
	 * @param endIdx   结束索引(1-len)-不包含
	 * @return 总值(超出整型最大值，则置为整型最大值)
	 */
	public static int sumArray(int[] attr, int startIdx, int endIdx) {
		if (attr == null || attr.length == 0) {
			return 0;
		}
		long sum = 0;
		for (int i = startIdx; i < endIdx; i++) {
			sum += (long) attr[i];
		}
		if (sum > Integer.MAX_VALUE) {
			Log.error("数组和超出整形最大值：", new Exception());
			sum = Integer.MAX_VALUE;
		}
		return (int) sum;
	}

	/**
	 * <pre>
	 * 计算数组和
	 * </pre>
	 *
	 * @param attr     long数组
	 * @param startIdx 起始索引(0-[len-1])-包含
	 * @param endIdx   结束索引(1-len)-不包含
	 * @return 总值(超出整型最大值，则置为整型最大值)
	 */
	public static long sumLongArray(long[] attr, int startIdx, int endIdx) {
		if (attr == null || attr.length == 0) {
			return 0;
		}
		long sum = 0;
		for (int i = startIdx; i < endIdx; i++) {
			sum += attr[i];
		}
		// 有可能本来就为负值
		// if (sum < 0) {
		// Log.error("数组和超出long最大值：", new Exception());
		// sum = Long.MAX_VALUE;
		// }
		return sum;
	}

	/**
	 * <pre>
	 * 是否为正整数
	 * </pre>
	 *
	 * @param str
	 * @return
	 */
	public final static boolean isPostiveInt(String str) {
		if (str == null) {
			return false;
		}
		return str.matches("^[0-9]+[0-9]*[0-9]*$");
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static int getIntValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		if (dataMap == null) {
			return 0;
		}
		Object pre = dataMap.get(key);
		int value = 0;
		if (pre != null) {
			// 整形
			if (pre instanceof Integer) {
				value = ((Integer) pre).intValue();
			}
			// 短整型
			else if (pre instanceof Short) {
				value = ((Short) pre).intValue();
			}
			// 长整型
			else if (pre instanceof Long) {
				long temp = ((Long) pre).longValue();
				if (temp > Integer.MAX_VALUE) {
					value = Integer.MAX_VALUE;
				} else {
					value = ((Long) pre).intValue();
				}
			}
			// 长整型
			else if (pre instanceof Float) {
				float temp = ((Float) pre).floatValue();
				if (temp > Integer.MAX_VALUE) {
					value = Integer.MAX_VALUE;
				} else {
					value = ((Float) pre).intValue();
				}
			} else if (pre instanceof Double) {
				double temp = ((Double) pre).floatValue();
				if (temp > Integer.MAX_VALUE) {
					value = Integer.MAX_VALUE;
				} else {
					value = ((Double) pre).intValue();
				}
			}
			// 其余数字类型
			else if (pre instanceof Number) {
				return ((Number) value).intValue();
			}
			// 其他
			else {
				Log.error("获取模块某数值时，发现原数据非整数，module:" + module.getClass().getSimpleName() + ",key:" + key + ",value:"
						+ pre + ",userId:" + userId);
				return 0;
			}
		}
		return value;
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static Number getNumberValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		if (dataMap == null) {
			return 0;
		}
		Object pre = dataMap.get(key);
		Number value = 0;
		if (pre != null) {
			value = (Number) pre;
		}
		return value;
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param dataList
	 * @param listIdx
	 * @param module
	 * @return
	 */
	public final static int getIntValue(List<Object> dataList, int listIdx, Object module) {
		if (listIdx < 0 || listIdx > dataList.size() - 1) {
			Log.error("超出List索引大小,listKey:" + listIdx + ",size:" + dataList.size() + ",module:" + module);
			return 0;
		}
		Object pre = dataList.get(listIdx);
		int value = 0;
		if (pre != null) {
			// 整形
			if (pre instanceof Integer) {
				value = ((Integer) pre).intValue();
			}
			// 短整型
			else if (pre instanceof Short) {
				value = ((Short) pre).intValue();
			}
			// 长整型
			else if (pre instanceof Long) {
				long temp = ((Long) pre).longValue();
				if (temp > Integer.MAX_VALUE) {
					value = Integer.MAX_VALUE;
				} else {
					value = ((Long) pre).intValue();
				}
			}
			// 其他
			else {
				Log.error("获取模块某数值时，发现原数据非整数，module:" + module.getClass().getSimpleName() + ",key:" + listIdx);
				return 0;
			}
		}
		return value;
	}

	public final static long getLongValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		return getLongValue(dataMap, key, module.getClass(), userId);
	}

	/**
	 * <pre>
	 * 获取长整形值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static long getLongValue(Map<String, Object> dataMap, String key, Class<?> module, int userId) {
		Object value = dataMap.get(key);
		if (value == null) {
			return 0;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value instanceof String) {
			String strVal = (String) value;
			if (strVal.length() == 0) {
				return 0;
			}

			if ("null".equals(strVal) || "NULL".equals(strVal)) {
				return 0;
			}

			try {
				return Long.parseLong(strVal);
			} catch (NumberFormatException ex) {
				//
			}

			JSONScanner dateParser = new JSONScanner(strVal);
			Calendar calendar = null;
			if (dateParser.scanISO8601DateIfMatch(false)) {
				calendar = dateParser.getCalendar();
			}
			dateParser.close();

			if (calendar != null) {
				return calendar.getTimeInMillis();
			}
		}
		Log.error("获取模块某数值时，发现原数据非数字，module:" + module.getSimpleName() + ",key:" + key + ",userId:" + userId);
		return 0;
	}

	public final static double getDoubleValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		return getDoubleValue(dataMap, key, module.getClass(), userId);
	}

	/**
	 * <pre>
	 * 获取双精度数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static double getDoubleValue(Map<String, Object> dataMap, String key, Class<?> module, int userId) {
		Object value = dataMap.get(key);
		if (value == null) {
			return 0;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		if (value instanceof String) {
			String strVal = (String) value;
			if (strVal.length() == 0) {
				return 0;
			}

			if ("null".equals(strVal) || "NULL".equals(strVal)) {
				return 0;
			}

			try {
				return Double.parseDouble(strVal);
			} catch (NumberFormatException ex) {
				//
			}

			JSONScanner dateParser = new JSONScanner(strVal);
			Calendar calendar = null;
			if (dateParser.scanISO8601DateIfMatch(false)) {
				calendar = dateParser.getCalendar();
			}
			dateParser.close();

			if (calendar != null) {
				return calendar.getTimeInMillis();
			}
		}
		Log.error("获取模块某数值时，发现原数据非数字，module:" + module.getSimpleName() + ",key:" + key + ",userId:" + userId);
		return 0;
	}

	public final static float getFloatValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		return getFloatValue(dataMap, key, module.getClass(), userId);
	}

	/**
	 * <pre>
	 * 获取单精度数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static float getFloatValue(Map<String, Object> dataMap, String key, Class<?> module, int userId) {
		Object value = dataMap.get(key);
		if (value == null) {
			return 0;
		}
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		if (value instanceof String) {
			String strVal = (String) value;
			if (strVal.length() == 0) {
				return 0;
			}

			if ("null".equals(strVal) || "NULL".equals(strVal)) {
				return 0;
			}

			try {
				return Float.parseFloat(strVal);
			} catch (NumberFormatException ex) {
				//
			}

			JSONScanner dateParser = new JSONScanner(strVal);
			Calendar calendar = null;
			if (dateParser.scanISO8601DateIfMatch(false)) {
				calendar = dateParser.getCalendar();
			}
			dateParser.close();

			if (calendar != null) {
				return calendar.getTimeInMillis();
			}
		}
		Log.error("获取模块某数值时，发现原数据非数字，module:" + module.getSimpleName() + ",key:" + key + ",userId:" + userId);
		return 0;
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static String getStringValue(Map<String, Object> dataMap, String key, Object module, int userId) {
		if (dataMap == null) {
			return "";
		}
		Object pre = dataMap.get(key);
		String value = "";
		if (pre != null) {
			// 整形
			if (pre instanceof String) {
				value = (String) pre;
			}
			// 其他
			else {
				Log.error("获取模块某数值时，发现原数据非整数，module:" + module.getClass().getSimpleName() + ",key:" + key + ",value:"
						+ pre + ",userId:" + userId);
				return "";
			}
		}
		return value;
	}

	/**
	 * <pre>
	 * 查找值在数组中的位置
	 * </pre>
	 *
	 * @param array
	 * @param val
	 * @return
	 */
	public final static int findArrIndex(int[] array, int val) {
		if (array == null || array.length < 1) {
			return -1;
		}
		for (int i = 0, len = array.length; i < len; i++) {
			if (val == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <pre>
	 * 是否数字
	 * </pre>
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// getRedBagFixedRewards(1);
		// for (int i = 0; i < 100; i++) {
		// getRedBagRewards(100, 10, 1);
		// }
		// System.out.println(doubleEquals(1.0001d, 1));
		// System.out.println(doubleEquals(1.0001d, 1d));
		// System.out.println(doubleEquals(1.0001d, 1.0001d));
		// System.out.println(3f / 6 == 2f / 4d);
		// System.out.println(findPrimeNums(1103));
		long st = System.currentTimeMillis();
		findPrimeNums(1000000);
		long ct = System.currentTimeMillis() - st;
		System.out.println(ct + "ms");
		// System.out.println(findPrimeNums2(10000));

	}

	/**
	 * <pre>
	 * 获取阶段的个数
	 * 比如: 获取445566的第一个阶段的数量6
	 * </pre>
	 *
	 * @param data
	 * @param stage
	 * @return
	 */
	public static int getStageValue(int data, int stage) {
		int r = 0;
		int d = 1;
		for (int i = 1; i < stage; i++) {
			d *= 10;
		}
		r = data / d;
		r %= 10;
		return r;
	}

	/**
	 * <pre>
	 * 设置阶段个数
	 * ! 注意, 设置的数据不能大于9且不能小于0
	 * ! 在设置完数据之后, 判断一下是否和原值相等, 相等则表示设置失败
	 * </pre>
	 *
	 * @param data     数据
	 * @param stage    阶段
	 * @param setValue 需要设置某阶段的数据
	 * @return
	 */
	public static int setStageValue(int data, int stage, int setValue) {
		if (setValue > 9 || setValue < 0) {
			return data;
		}
		int proValue = getStageValue(data, stage);
		data = removeStageValeu(data, stage, proValue, true);
		data = addSetageValue(data, stage, setValue, true);
		return data;
	}

	/**
	 * <pre>
	 * 增加阶段数
	 * ! 在设置完数据之后, 判断一下是否和原值相等, 相等则表示设置失败
	 * </pre>
	 *
	 * @param data
	 * @param stage
	 * @param addValue
	 * @param isMaxValue 如果超过单个阶段最大值9是否设置为最大值
	 * @return
	 */
	public static int addSetageValue(int data, int stage, int addValue, boolean isMaxValue) {
		int proValue = getStageValue(data, stage);
		// 如:17 = 6 + 9;
		int newValue = proValue + addValue;
		if (newValue > 9) {
			if (isMaxValue) {
				// 如:8 = 9 - 6;
				addValue = 9 - proValue;
			}
			// 不设置为最大值直接返回false
			else {
				return data;
			}
		}
		for (int i = 1; i < stage; i++) {
			addValue *= 10;
		}
		data += addValue;
		return data;
	}

	/**
	 * <pre>
	 * 移除阶段数
	 * !  在设置完数据之后, 判断一下是否和原值相等, 相等则表示设置失败
	 * </pre>
	 *
	 * @param data
	 * @param stage
	 * @param removeValue
	 * @param isMinValue  如果移除后单个阶段值小于最小值0是否设置为最小值
	 * @return
	 */
	public static int removeStageValeu(int data, int stage, int removeValue, boolean isMinValue) {
		int proValue = getStageValue(data, stage);
		int newValue = proValue - removeValue;
		if (newValue < 0) {
			if (isMinValue) {
				removeValue = proValue;
			}
			// 不设置为最大值直接返回false
			else {
				return data;
			}
		}
		for (int i = 1; i < stage; i++) {
			removeValue *= 10;
		}
		data -= removeValue;
		return data;
	}

	/**
	 * <pre>
	 * 比较浮点型
	 * </pre>
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public final static boolean doubleEquals(double a, double b) {
		return (int) (a * 1000000000d) == (int) (b * 1000000000d);
	}

	/**
	 * <pre>
	 * 获取小于等于n的所有质数
	 * </pre>
	 *
	 * @param max
	 * @return
	 */
	public final static List<Integer> findPrimeNums(int max) {
		List<Integer> primes = new ArrayList<Integer>(max / 2);
		primes.add(2);
		for (int i = 3; i <= max; i++) {
			if (i % 2 == 0) {
				continue;
			}
			int tmp = (int) Math.sqrt(i) + 1;
			for (int j = 2; j <= tmp; j++) {
				if (i % j == 0)
					break;
				if (j == tmp)
					primes.add(i);
			}
		}
		return primes;
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param dataMap
	 * @param key
	 * @param module
	 * @return
	 */
	public final static int getIntValue(Map<String, Map<String, Object>> dataMap, String moduleId, String key,
			Object module, int userId) {
		Map<String, Object> jsonMap = dataMap.get(moduleId);
		if (jsonMap == null) {
			return 0;
		}
		Object pre = jsonMap.get(key);
		int value = 0;
		if (pre != null) {
			// 整形
			if (pre instanceof Integer) {
				value = ((Integer) pre).intValue();
			}
			// 短整型
			else if (pre instanceof Short) {
				value = ((Short) pre).intValue();
			}
			// 长整型
			else if (pre instanceof Long) {
				long temp = ((Long) pre).longValue();
				if (temp > Integer.MAX_VALUE) {
					value = Integer.MAX_VALUE;
				} else {
					value = ((Long) pre).intValue();
				}
			}
			// 其他
			else {
				Log.error("增加模块某数值时，发现原数据非整数，module:" + module.getClass().getSimpleName() + ",key:" + key + ",value:"
						+ pre + ",userId:" + userId);
				return 0;
			}
		}
		return value;
	}

}
