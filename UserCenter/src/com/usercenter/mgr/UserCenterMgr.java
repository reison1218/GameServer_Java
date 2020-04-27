package com.usercenter.mgr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.utils.Log;
import com.usercenter.entity.GameInfo;
import com.usercenter.entity.GameInfoDao;
import com.usercenter.entity.UserInfoDao;

/**
 * 用户中心mgr
 * @author reison
 *
 */
public class UserCenterMgr {
	
	/**玩家id初始值**/
	public static final int INIT_USEER_ID = 1000000;

	/**游戏infomap，key:gameid value:GameInfo**/
	private static Map<Integer,GameInfo> gameMap = new ConcurrentHashMap<Integer,GameInfo>();
	/**游戏对应的最大玩家id key:gameId value:userId**/
	private static Map<Integer,AtomicInteger> gameUserMap = new ConcurrentHashMap<Integer,AtomicInteger>();
	
	/**
	 * 获取该游戏最大的玩家id
	 * @param gameId
	 * @param needInit 是否需要初始化
	 * @return
	 */
	public static AtomicInteger getMaxUserId(int gameId,boolean needInit) {
		if(!gameUserMap.containsKey(gameId) && needInit) {
			String idStr = gameId+""+UserCenterMgr.INIT_USEER_ID;
			gameUserMap.put(gameId, new AtomicInteger(Integer.parseInt(idStr)));
		}
		return gameUserMap.get(gameId);
	}
	
	/**
	 * 初始化游戏列表
	 * @return
	 */
	public static boolean init() {
		try {
			gameMap = GameInfoDao.getInstance().findGameInfos();
			gameUserMap = UserInfoDao.getInstance().findMaxUserId();
		}catch(Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否有这个游戏
	 * @param gameId
	 * @return
	 */
	public static boolean hasGame(int gameId) {
		return gameMap.containsKey(gameId);
	}
	
	
	
}
