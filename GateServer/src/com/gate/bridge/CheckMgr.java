/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.utils.Log;


/**
 * <pre>
 * 网关包检测
 * </pre>
 * 
 * @author reison
 * @time 2017年12月18日 下午5:10:29
 */
public final class CheckMgr {

	/** 需检测的协议号 */
	private final static Map<Short, Long> checkCode = new HashMap<>();

	/** 客户端数据包频率检测缓存<uid,<code,secs>> */
	private final static Map<Integer, Map<Integer, Long>> pkgCheckMap = new ConcurrentHashMap<>();

	static {
		// 客户端攻击间隔为600ms，服务端检测为500ms
		//checkCode.put(GameServerCode.LOG_OFF, 500l);
		// checkCode.put(BaseFightCode.MOVE, 500l);
	}

	// private final static AtomicInteger c = new AtomicInteger();

	/**
	 * <pre>
	 * 包频率检测
	 * </pre>
	 *
	 * @param userId
	 * @param code
	 * @param rateMills
	 * @return
	 */
	public final static boolean checkPkgRate(int userId, int code) {
		Long rateMills = checkCode.get(code);
		if (rateMills == null) {
			return true;
		}
		Map<Integer, Long> userMap = pkgCheckMap.get(userId);
		if (userMap == null) {
			userMap = new ConcurrentHashMap<>();
			pkgCheckMap.put(userId, userMap);
		}
		long nowMills = System.currentTimeMillis();
		Long lastMills = userMap.get(code);
		if (lastMills == null) {
			lastMills = 0l;
		}
		userMap.put(code, nowMills);
		if (nowMills - lastMills < rateMills) {
			Log.info("find_pkg_rate_illegal,type:" + code + ",userId:" + userId);
			// 跨服门派战取消网关频率检测失败日志
			// int cc = c.incrementAndGet();
			// if (cc % 10000 == 0) {
			// Log.info("非法次数达到" + cc);
			// }
			return false;
		}
		return true;
	}

	/**
	 * <pre>
	 * 卸载数据
	 * </pre>
	 *
	 * @param userId
	 */
	public final static void unload(int userId) {
		pkgCheckMap.remove(userId);
	}

	public static void main(String[] args) throws Exception {
		Log.init(CheckMgr.class);
		checkPkgRate(1, (short) 10005);
		Thread.sleep(499);
		checkPkgRate(1, (short) 10005);
		Thread.sleep(40);
		checkPkgRate(1, (short) 10005);
	}

}
