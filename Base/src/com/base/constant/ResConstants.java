package com.base.constant;

import java.util.HashMap;
import java.util.Map;

import com.base.key.PlayerKey;

/**
 * 资源定义接口
 * 
 * @author reison
 *
 */
public class ResConstants {

	public static Map<Integer, String> resMap = new HashMap<Integer, String>();

	// ---------------------------------资源类型-----------------------------------
	/** 物品 **/
	public static final int GOODS = 1;
	/** 资源（货币之类的） **/
	public static final int RES = 2;

	// ---------------------------------资源子类型--------------------------------------
	/** 金币 **/
	public static final int GOLD = 1;
	/** 钻石 **/
	public static final int DIAMONDS = 2;
	/** 转盘次数 **/
	public static final int TURN_COUNT = 3;

	static {
		resMap.put(GOLD, PlayerKey.GOLD);
		resMap.put(DIAMONDS, PlayerKey.DIAMONDS);
		resMap.put(TURN_COUNT, PlayerKey.TURN_COUNT);
	}

	public static String getPlayerKey(int id) {
		return resMap.get(id);
	}
}
