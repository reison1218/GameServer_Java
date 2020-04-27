/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import sun.net.util.IPAddressUtil;

/**
 * <pre>
 * IP工具
 * </pre>
 * 
 * @author jiahao.fang
 * @time 2017年6月16日 下午5:40:27
 */
public class IpUtil {

	/** 高德Key */
	private final static String LBS_KEY = "095a7a68f339a7a8f3a2cc5f1d33464d";

	/** 高德IP定位API服务地址 */
	private final static String LBS_IP_HTTP = "http://restapi.amap.com/v3/ip";

	/** 请求字段 */
	private final static String REQ_IP_FIELD = "ip";
	private final static String REQ_KEY_FIELD = "key";

	/** 响应字段 */
	private final static String RESP_CITY_FIELD = "city";
	private final static String RESP_PROVINCE_FIELD = "province";
	private final static String RESP_STATUS_FIELD = "status";
	private final static String RESP_RECTANGLE = "rectangle";

	/**
	 * <pre>
	 * 成功返回示例：
	 * {"status":"1","info":"OK","infocode":"10000","province":"广东省","city":"深圳市","adcode":"440300","rectangle":"113.9629412,22.4627142;114.2106056,22.61394155"}
	 * </pre>
	 *
	 * @param ip
	 * @return
	 */
	private static String ipSearch(String ip) {
		Map<String, Object> map = new HashMap<>();
		map.put(REQ_KEY_FIELD, LBS_KEY);
		// 如果是内网，不填ip，高德返回访问ip的地址
		if (!isLanIp(ip)) {
			map.put(REQ_IP_FIELD, ip);
		}
		String s = HttpUtil.doGet(LBS_IP_HTTP, map);
		return s;
	}

	/**
	 * <pre>
	 * 通过ip获得经纬度
	 * </pre>
	 *
	 * @param ip
	 * @return
	 */
	public static double[] ip2Coordinate(String ip) {
		double[] coordinate = new double[2];
		try {
			String respon = ipSearch(ip);
			JSONObject result = (JSONObject) JsonUtil.parse(respon);
			double[][] rectangle_list = SplitUtil.parseDoubleArrays(result.getString(RESP_RECTANGLE), ";", ",");
			coordinate[0] = (rectangle_list[0][0] + rectangle_list[1][0]) / 2;
			coordinate[1] = (rectangle_list[0][1] + rectangle_list[1][1]) / 2;
		} catch (Exception e) {
			Log.error("IP地址查找位置失败,ip:" + ip, e);
		}
		return coordinate;
	}

	/**
	 * <pre>
	 * 根据IP查找城市
	 * </pre>
	 *
	 * @param ip
	 * @return
	 */
	public static String ip2City(String ip) {
		try {
			String respon = ipSearch(ip);
			JSONObject o = (JSONObject) JsonUtil.parse(respon);
			if (o.getIntValue(RESP_STATUS_FIELD) == 1) {
				String city = o.get(RESP_CITY_FIELD).toString().replace(" ", "");
				if (ChineseUtil.isChineseByReg(city)) {
					return city;
				}
				return "";
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log.error("IP地址查找城市失败。");
		}
		return "";
	}

	/**
	 * <pre>
	 * 根据IP查找省
	 * </pre>
	 *
	 * @param ip
	 * @return
	 */
	public static String ip2Province(String ip) {
		try {
			String respon = ipSearch(ip);
			JSONObject o = (JSONObject) JsonUtil.parse(respon);
			if (o.getIntValue(RESP_STATUS_FIELD) == 1) {
				String province = o.get(RESP_CITY_FIELD).toString().replace(" ", "");;
				if (ChineseUtil.isChineseByReg(province)) {
					return province;
				}
				return o.get(RESP_PROVINCE_FIELD).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("IP地址查找省失败。");
		}
		return "";
	}

	public static void main(String[] args) {
		boolean b = isLanIp("10.10.10.196");
		String s = ip2City("113.102.163.228");
		String s2 = ip2City("113.105.7.21");
		double[] s3 = ip2Coordinate("113.105.7.21");
		System.out.println(s);
		System.out.println(s2);
		System.out.println(b);
		System.out.println(Arrays.toString(s3));
	}

	/**
	 * <pre>
	 * 是否为内网IP
	 * </pre>
	 *
	 * @param ip
	 * @return
	 */
	public static boolean isLanIp(String ip) {
		byte[] addr = IPAddressUtil.textToNumericFormatV4(ip);
		return internalIp(addr);
	}

	private static boolean internalIp(byte[] addr) {
		final byte b0 = addr[0];
		final byte b1 = addr[1];
		// 10.x.x.x/8
		final byte SECTION_1 = 0x0A;
		// 172.16.x.x/12
		final byte SECTION_2 = (byte) 0xAC;
		final byte SECTION_3 = (byte) 0x10;
		final byte SECTION_4 = (byte) 0x1F;
		// 192.168.x.x/16
		final byte SECTION_5 = (byte) 0xC0;
		final byte SECTION_6 = (byte) 0xA8;
		switch (b0) {
		case SECTION_1:
			return true;
		case SECTION_2:
			if (b1 >= SECTION_3 && b1 <= SECTION_4) {
				return true;
			}
		case SECTION_5:
			switch (b1) {
			case SECTION_6:
				return true;
			}
		default:
			return false;

		}
	}

}
