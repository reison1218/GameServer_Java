/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.game.code.impl.AnnReqCode;
import com.utils.ClassUtil;
import com.utils.Log;



/**
 * <pre>
 * 协议号管理
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class CodeMgr {

	/** 客户端请求协议号注解缓存 */
	private final static Map<Integer, AnnReqCode> reqCodeMap = new HashMap<>();

	/**
	 * <pre>
	 * 初始化
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean init() {
		AnnReqCode reqAnn = null;
		Set<Class<?>> allClasses = ClassUtil.getClasses(CodeLocation.class.getPackage());
		for (Class<?> clazz : allClasses) {
			try {
				Field[] fs = clazz.getDeclaredFields();
				for (int i = 0, len = fs.length; i < len; i++) {
					Field f = fs[i];
					int code = 0;
					try {
						code = f.getInt(null);
					} catch (Exception e) {
						continue;
					}
					reqAnn = f.getAnnotation(AnnReqCode.class);
					if (reqAnn != null) {
						AnnReqCode preAnn = reqCodeMap.get(code);
						if (preAnn != null) {
							Log.error("存在重复协议号：" + code);
							return false;
						}
						reqCodeMap.put(code, reqAnn);
					}
				}
			} catch (Exception e) {
				Log.error("Load module obj error,name : " + clazz.getSimpleName(), e);
				return false;
			}
		}
		Log.info("协议号初始化成功，数量：" + reqCodeMap.size());
		return true;
	}

	/**
	 * <pre>
	 * 获取协议号注解
	 * </pre>
	 *
	 * @param code
	 * @return
	 */
	public final static AnnReqCode getCodeAnn(int code) {
		return reqCodeMap.get(code);
	}
}
