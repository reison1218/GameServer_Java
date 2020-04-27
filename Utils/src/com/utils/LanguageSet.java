package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 语言包
 * </pre>
 * 
 * @author reison
 * @time 2017年3月20日 下午5:25:02
 */
public final class LanguageSet {

	/** 语言包文件路径 */
	private static String path = "";
	/** 语言包缓存 */
	private static final Map<String, String> langMap = new HashMap<String, String>();

	/**
	 * <pre>
	 * 初始化
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean init(String path) {
		if (path == null || path.equals("")) {
			return false;
		}
		LanguageSet.path = path;
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(isr);
			String lineContent;
			int sum = 0;
			while ((lineContent = bufferedReader.readLine()) != null) {
				int index = lineContent.indexOf("=");
				if (index > -1) {
					String[] tempArr = lineContent.split("=");
					if (tempArr.length < 2 || tempArr.length > 2) {
						Log.error("存在语言包格式错误，语言包每一行有且只能有一个=，errorLine:" + lineContent);
						continue;
					}
					sum += lineContent.length();
					langMap.put(tempArr[0], tempArr[1]);
				}
			}
			bufferedReader.close();
			isr.close();
			fis.close();
			Log.info("语言包加载完成,总条数:" + langMap.size() + ",总长度：" + sum);
			return true;
		} catch (FileNotFoundException e) {
			Log.error("语言包资源文件不存在", e);
		} catch (IOException e) {
			Log.error("语言包资源文件加载失败", e);
		}
		return false;
	}

	/**
	 * <pre>
	 * 重新加载语言包
	 * </pre>
	 *
	 * @return
	 */
	public static boolean reloadLanguageResource() {
		langMap.clear();
		return init(path);
	}

	/**
	 * <pre>
	 * 获取语言包(有参数)
	 * </pre>
	 *
	 * @param key
	 * @param paras
	 * @return
	 */
	public static String getResource(String key, Object... paras) {
		String initValue = langMap.get(key);
		langMap.containsKey("GameServer.MingsuTowerInventory.notice");
		if (initValue == null) {
			Log.error("语言包中不存在,key:" + key);
			return "";
		}
		try {
			initValue = initValue.replace("'", "@");
			String msg = MessageFormat.format(initValue, paras);
			return msg.replace("@", "'");
		} catch (Exception e) {
			Log.error("", e);
		}
		return "";
	}

	/**
	 * <pre>
	 * 获取语言包(无参数)
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public static String getResource(String key) {
		String msg = langMap.get(key);
		if (msg == null) {
			msg = "";
		}
		return msg;
	}

	/**
	 * <pre>
	 * 获取语言包
	 * ! 以","号分割的
	 * </pre>
	 *
	 * @param key
	 * @return String[]
	 */
	public static String[] getResourceArr(String key) {
		String msg = langMap.get(key);
		if (msg == null) {
			Log.error("语言包配置有误");
			msg = "";
		}
		return msg.split(",");
	}

}
