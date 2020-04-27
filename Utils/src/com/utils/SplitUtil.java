package com.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 字符串切割工具类
 * </pre>
 * 
 * @author reison
 * @time 2017年4月5日 下午5:47:16
 */
public class SplitUtil {

	private SplitUtil() {
	}

	public static int[] splitToInt(String str) {
		return splitToInt(str, ",");
	}

	public static long[] splitToLong(String str) {
		return splitToLong(str, ",");
	}

	public static int[] splitToInt(String str, String spStr) {
		if (str == null || str.trim().length() == 0) {
			return new int[0];
		}

		try {
			String[] temps = str.split(spStr);
			int len = temps.length;
			int[] results = new int[len];
			for (int i = 0; i < len; i++) {
				if (temps[i].trim().length() > 0) {
					results[i] = Integer.parseInt(temps[i].trim());
				}
			}
			return results;
		} catch (Exception e) {
			return new int[0];
		}
	}

	public static long[] splitToLong(String str, String spStr) {
		if (str == null || str.trim().length() == 0) {
			return new long[0];
		}

		try {
			String[] temps = str.split(spStr);
			int len = temps.length;
			long[] results = new long[len];
			for (int i = 0; i < len; i++) {
				if (temps[i].trim().length() > 0) {
					results[i] = Long.parseLong(temps[i].trim());
				}
			}
			return results;
		} catch (Exception e) {
			Log.error("str :" + str, e);
			return new long[0];
		}
	}

	public static double[] splitToDouble(String str, String spStr) {
		if (str == null || str.trim().length() == 0) {
			return new double[0];
		}

		try {
			String[] temps = str.split(spStr);
			int len = temps.length;
			double[] results = new double[len];
			for (int i = 0; i < len; i++) {
				if (temps[i].trim().length() > 0) {
					results[i] = Double.parseDouble(temps[i].trim());
				}
			}
			return results;
		} catch (Exception e) {
			Log.error("str :" + str, e);
			return new double[0];
		}
	}

	public static String[] splitToStr(String str) {
		return splitToStr(str, ",");
	}

	public static String[] splitToStr(String str, String spStr) {
		if (str == null || str.trim().length() == 0) {
			return new String[0];
		}

		try {
			String[] temps = str.split(spStr);
			int len = temps.length;
			String[] results = new String[len];
			for (int i = 0; i < len; i++) {
				if (temps[i].trim().length() > 0) {
					results[i] = String.valueOf((temps[i].trim()));
				}
			}
			return results;
		} catch (Exception e) {
			Log.error("str :" + str, e);
			return new String[0];
		}
	}

	public static String concatToStrByFloat(float[] ints) {
		if (ints == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ints.length; i++) {
			sb.append(ints[i]).append(",");
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String concatToStr(int[] ints) {
		if (ints == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ints.length; i++) {
			sb.append(ints[i]).append(",");
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static void print(int[] results) {
		for (int i = 0; i < results.length; i++) {
			System.err.print(results[i] + ",");
		}
	}

	public static void print(String str) {
		System.err.println(str);
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher isNum = pattern.matcher(str);
		return isNum.matches();
	}

	/**
	 * 解析英雄模板ID
	 * 
	 * @param heros
	 * @return
	 */
	public static int[] parseHeroTemplate(String heros) {
		if (heros == null || heros.trim().length() == 0)
			return null;
		if (heros != null && heros.trim().length() > 0) {
			String[] heroArray = heros.split("[|]");
			if (heroArray.length > 0) {
				int[] heroTemplateIds = new int[heroArray.length];
				int idx = 0;
				for (String idAndPos : heroArray) {
					String[] idPos = idAndPos.split("[,]");
					heroTemplateIds[idx] = Integer.parseInt(idPos[0]);
					idx++;
				}
				return heroTemplateIds;
			}
		}
		return null;
	}

	/**
	 * <pre>
	 * 1,2|1,3|2,5
	 * </pre>
	 *
	 * @param src
	 * @param separator1
	 * @param separator2
	 * @return
	 */
	public static int[][] parseIntArray(String src, String separator1, String separator2) {
		String[] strs = src.split("[" + separator1 + "]");

		int[][] buffInfos = new int[strs.length][2];

		for (int i = 0; i < strs.length; i++) {

			String[] regs = strs[i].split(separator2);

			buffInfos[i][0] = Integer.parseInt(regs[0]);
			buffInfos[i][1] = Integer.parseInt(regs[1]);
		}
		return buffInfos;
	}

	public static int[][] parseIntArrays(String src, String separator1, String separator2) {
		if (src == null || src.trim().length() == 0) {
			return new int[0][0];
		}

		try {
			String[] strs = src.split("[" + separator1 + "]");
			separator2 = "[" + separator2 + "]";
			int[][] buffInfos = null;
			String[] regs = null;
			for (int i = 0; i < strs.length; i++) {

				regs = strs[i].split(separator2);
				if (buffInfos == null) {
					buffInfos = new int[strs.length][regs.length];
				}
				if (regs.length > 1) {
					for (int j = 0; j < regs.length; j++) {
						buffInfos[i][j] = Integer.parseInt(regs[j]);
					}
				}
			}
			return buffInfos;
		} catch (Exception e) {
			Log.error("src :" + src, e);
			return new int[0][0];
		}
	}

	public static double[][] parseDoubleArrays(String src, String separator1, String separator2) {
		String[] strs = src.split("[" + separator1 + "]");
		separator2 = "[" + separator2 + "]";
		double[][] buffInfos = null;
		String[] regs = null;
		for (int i = 0; i < strs.length; i++) {

			regs = strs[i].split(separator2);
			if (buffInfos == null) {
				buffInfos = new double[strs.length][regs.length];
			}

			for (int j = 0; j < regs.length; j++) {
				buffInfos[i][j] = Double.parseDouble(regs[j]);
			}
		}
		return buffInfos;
	}

	public static String[][] parseStringArrays(String src) {
		return parseStringArrays(src, "|", ",");
	}

	public static String[][] parseStringArrays(String src, String separator1, String separator2) {
		String[] strs = src.split("[" + separator1 + "]");
		separator2 = "[" + separator2 + "]";
		String[][] buffInfos = null;
		String[] regs = null;
		for (int i = 0; i < strs.length; i++) {

			regs = strs[i].split(separator2);
			if (buffInfos == null) {
				buffInfos = new String[strs.length][regs.length];
			}

			for (int j = 0; j < regs.length; j++) {
				buffInfos[i][j] = regs[j];
			}
		}
		return buffInfos;
	}

	/************************** 增加long值 **************************/
	public final static Map<Integer, Long> splitToMapLong(String str) {
		return splitToMapLong(str, null, null);
	}

	public final static Map<Integer, Long> splitToMapLong(String str, Long value) {
		return splitToMapLong(str, null, null, value);
	}

	public final static Map<Integer, Long> splitToMapLong(String str, String sep1, String sep2) {
		return splitToMapLong(str, sep1, sep2, null);
	}

	/**
	 * <pre>
	 * k1,v1|k2,v2|k3,v3
	 * 类似上述字符串格式化为Map
	 * </pre>
	 * 
	 * @param <Integer>
	 * @param <Integer>
	 * @param str
	 * @param sep1
	 * @param sep2
	 */
	public static Map<Integer, Long> splitToMapLong(String str, String sep1, String sep2, Long value) {
		Map<Integer, Long> map = new HashMap<>();
		if (str == null || str.trim().length() == 0) {
			return map;
		}
		str = str.trim();
		if (sep1 == null) {
			sep1 = "[|]";
		}
		if (sep2 == null) {
			sep2 = "[,]";
		}
		String[] sr1 = str.split(sep1);
		if (sr1 == null || sr1.length < 1) {
			return map;
		}
		String[] sr2 = null;
		for (int i = 0, len = sr1.length; i < len; i++) {
			sr2 = sr1[i].split(sep2);
			if (sr2.length < 2) {
				continue;
			}
			if (value != null) {
				for (int j = 0, lenj = sr2.length; j < lenj; j++) {
					map.put(Integer.parseInt(sr2[j]), value);
				}
			} else {
				map.put(Integer.parseInt(sr2[0]), Long.parseLong(sr2[1]));
			}
		}
		return map;
	}

	public final static Map<Integer, Integer> splitToMap(String str) {
		return splitToMap(str, null, null);
	}

	public final static Map<Integer, Integer> splitToMap(String str, Integer value) {
		return splitToMap(str, null, null, value);
	}

	public final static Map<Integer, Integer> splitToMap(String str, String sep1, String sep2) {
		return splitToMap(str, sep1, sep2, null);
	}

	/**
	 * <pre>
	 * k1,v1|k2,v2|k3,v3
	 * 类似上述字符串格式化为Map
	 * </pre>
	 * 
	 * @param <Integer>
	 * @param <Integer>
	 * @param str
	 * @param sep1
	 * @param sep2
	 */
	public static Map<Integer, Integer> splitToMap(String str, String sep1, String sep2, Integer value) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if (str == null || str.trim().length() == 0) {
			return map;
		}
		str = str.trim();
		if (sep1 == null) {
			sep1 = "[|]";
		}
		if (sep2 == null) {
			sep2 = "[,]";
		}
		String[] sr1 = str.split(sep1);
		if (sr1 == null || sr1.length < 1) {
			return map;
		}
		String[] sr2 = null;
		for (int i = 0, len = sr1.length; i < len; i++) {
			sr2 = sr1[i].split(sep2);
			if (sr2.length < 2) {
				continue;
			}
			if (value != null) {
				for (int j = 0, lenj = sr2.length; j < lenj; j++) {
					map.put(Integer.parseInt(sr2[j]), value);
				}
			} else {
				map.put(Integer.parseInt(sr2[0]), Integer.parseInt(sr2[1]));
			}
		}
		return map;
	}

	/**
	 * <pre>
	 * k1,v1|k2,v2|k3,v3
	 * 类似上述字符串格式化为Map
	 * </pre>
	 * 
	 * @param str
	 */
	public static Map<String, String> splitToStrMap(String str) {
		Map<String, String> map = new HashMap<String, String>();
		if (str == null || str.trim().length() == 0) {
			return map;
		}
		str = str.trim();
		String sep1 = "[|]";
		String sep2 = "[,]";
		String[] sr1 = str.split(sep1);
		if (sr1 == null || sr1.length < 1) {
			return map;
		}
		String[] sr2 = null;
		for (int i = 0, len = sr1.length; i < len; i++) {
			sr2 = sr1[i].split(sep2);
			if (sr2.length < 2) {
				continue;
			}
			map.put(sr2[0], sr2[1]);
		}
		return map;
	}

	public static String concatToStr(Collection<Integer> list) {
		return concatToStr(list, null);
	}

	/**
	 * <pre>
	 * list拼接成字符串
	 * </pre>
	 * 
	 * @param <Integer>
	 * @param list
	 * @param sep
	 * @return
	 */
	public static String concatToStr(Collection<Integer> list, String sep) {
		if (sep == null) {
			sep = ",";
		}
		StringBuilder sb = new StringBuilder();
		for (Integer v : list) {
			if (v != null) {
				sb.append(v);
				sb.append(sep);
			}
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String concatToStr(Map<Integer, Integer> map) {
		return concatToStr(null, null, map, false);
	}

	public static String concatToStrBySort(Map<Integer, Integer> map) {
		return concatToStr(null, null, map, true);
	}

	/**
	 * <pre>
	 * k1,v1|k2,v2|k3,v3
	 * 将Map转化为为类似以上格式的字符串
	 * </pre>
	 * 
	 * @param <Integer>
	 * @param <Integer>
	 * @param sep1
	 * @param sep2
	 * @param map
	 * @return
	 */
	public static String concatToStr(String sep1, String sep2, Map<Integer, Integer> map, boolean isSort) {
		if (sep1 == null) {
			sep1 = "|";
		}
		if (sep2 == null) {
			sep2 = ",";
		}
		if (isSort) {
			map = new TreeMap<Integer, Integer>(map);
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append(sep2);
			sb.append(entry.getValue());
			sb.append(sep1);
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public final static HashSet<Integer> splitToHashSet(String str) {
		HashSet<Integer> set = new HashSet<>();
		if (str == null) {
			return set;
		}
		str = str.replaceAll(" ", "");
		if (str.length() == 0) {
			return set;
		}
		String[] arr = str.split(",");
		for (int i = 0, len = arr.length; i < len; i++) {
			Integer value = 0;
			try {
				value = Integer.parseInt(arr[i]);
			} catch (Exception e) {
				Log.error("", e);
			}
			set.add(value);
		}
		return set;
	}

	public static int splitToSonType(String str) {
		return Integer.parseInt(str.substring(2));
	}

	public static List<String> splitToArrayList(String str, String spStr) {
		List<String> list = new ArrayList<String>();
		String[] strs = str.split(spStr);
		list = Arrays.asList(strs);
		return list;
	}

	public static String concatToStr(List<String> list, String separator) {
		if (separator.equals(",")) {
			return list.toString().substring(1, list.toString().length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s + separator);
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		}
		return "";
	}

	public final static List<Integer> splitToIntList(String str) {
		List<Integer> list = new ArrayList<Integer>();
		if (str == null) {
			return list;
		}
		str = str.replaceAll(" ", "");
		if (str.length() == 0) {
			return list;
		}
		String[] arr = str.split(",");
		for (int i = 0, len = arr.length; i < len; i++) {
			Integer value = 0;
			try {
				value = Integer.parseInt(arr[i]);
			} catch (Exception e) {
				Log.error("", e);
			}
			list.add(value);
		}
		return list;
	}

	public static void main(String[] args) {
		String src = "1,2,3,4,5,6|10,20,30,40,50,60";
		int[][] inss = parseIntArrays(src, "|", ",");
		for (int[] ins : inss) {
			for (int in : ins) {
				System.out.println(in);
			}
		}
	}
}
