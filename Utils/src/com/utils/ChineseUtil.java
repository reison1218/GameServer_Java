/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * <pre>
 * 随机取名工具
 * </pre>
 * 
 * @author reison
 * @time 2017年5月6日 下午2:57:59
 */
public final class ChineseUtil {

	/** 598 百家姓 */
	final static String[] SUR_NAMES = { "万俟", "司马", "上官", "欧阳", "夏侯", "诸葛", "闻人", "东方", "赫连", "皇甫", "羊舌", "尉迟", "公羊", "澹台", "公冶", "宗正", "濮阳", "淳于", "单于", "太叔", "公孙", "仲孙", "轩辕", "令狐", "钟离", "宇文", "长孙", "慕容", "鲜于", "闾丘", "司徒", "司空", "兀官", "司寇", "南门", "呼延", "子车", "颛孙", "端木", "巫马", "公西", "漆雕", "车正", "壤驷", "公良", "拓跋", "夹谷", "宰父", "谷梁", "段干", "百里", "东郭", "微生", "梁丘", "左丘", "东门", "西门", "南宫", "第五", "公仪",
			"公乘", "太史", "仲长", "叔孙", "屈突", "尔朱", "东乡", "相里", "胡母", "司城", "张廖", "雍门", "毋丘", "贺兰", "綦毋", "屋庐", "独孤", "南郭", "北宫", "王孙" };

	public final static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(randName("S1."));
		}
	}

	public final static String randName(String showSite) {
		return showSite + randSurName(1).append(randName(RandomUtil.rand(1, 3))).toString();
	}

	/**
	 * <pre>
	 * 随机姓氏
	 * </pre>
	 *
	 * @param len
	 * @return
	 */
	private final static StringBuilder randSurName(int len) {
		StringBuilder name = new StringBuilder(len);
		if (len > 8) {
			return name;
		}
		final int surLen = SUR_NAMES.length;
		for (int i = 0; i < len; i++) {
			name.append(SUR_NAMES[RandomUtil.rand(surLen)]);
		}
		return name;
	}

	/**
	 * <pre>
	 * 随机汉字
	 * </pre>
	 *
	 * @param len
	 * @return
	 */
	private final static StringBuilder randName(int len) {
		String str = "";
		StringBuilder name = new StringBuilder(len);
		if (len > 8) {
			return name;
		}
		for (int i = 0; i < len; i++) {
			int highPos = 176 + Math.abs(RandomUtil.rand(71));// 区码，0xA0打头，从第16区开始，即0xB0=11*16=176,16~55一级汉字，56~87二级汉字
			int lowPos = 161 + Math.abs(RandomUtil.rand(94));// 位码，0xA0打头，范围第1~94列

			byte[] bArr = new byte[2];
			bArr[0] = (new Integer(highPos)).byteValue();
			bArr[1] = (new Integer(lowPos)).byteValue();
			try {
				str = new String(bArr, "GB2312"); // 区位码组合成汉字
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			name.append(str);
		}
		return name;
	}

	/**
	 * <pre>
	 * 判断是否全是中字，不含标点符号
	 * </pre>
	 *
	 * @param str
	 * @return
	 */
	public static boolean isChineseByReg(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
		return pattern.matcher(str).matches();
	}

}
