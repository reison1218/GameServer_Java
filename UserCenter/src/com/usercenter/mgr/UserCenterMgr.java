package com.usercenter.mgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import com.usercenter.entity.GameInfo;
import com.usercenter.entity.GameInfoDao;
import com.usercenter.entity.ServerInfo;
import com.usercenter.entity.ServerInfoDao;
import com.usercenter.entity.UserInfo;
import com.usercenter.entity.UserInfoDao;
import com.utils.Log;

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
	/**用户信息缓存队列,定时器持久化到数据库，延迟执行 key:gameId value:userId**/
	private static LinkedBlockingDeque<UserInfo> userQue = new LinkedBlockingDeque<UserInfo>();
	/**服务器列表信息**/
	private static Map<Integer,List<ServerInfo>> serverMap = new HashMap<Integer, List<ServerInfo>>();
	
	/**
	 * 向队列头部塞入userinfo
	 * @param userInfo
	 */
	public static void addUserInfo(UserInfo userInfo) {
		userQue.push(userInfo);
	}
	
	/**
	 * 根据游戏id获得游戏信息
	 * @param gameId
	 * @return
	 */
	public static GameInfo getGameInfo(int gameId) {
		return gameMap.get(gameId);
	}
	
	/**
	 * 返回所有游戏信息
	 * @return
	 */
	public static Map<Integer,GameInfo> getGameInfos(){
		return gameMap;
	}
	
	
	/**
	 * <pre>
	 * 保存数据
	 * </pre>
	 */
	public static int save() {
		int size = 0;
		while(!userQue.isEmpty()) {
			UserInfo userInfo = userQue.pollLast();
			if(userInfo == null) {
				Log.warn("userInfo is null!");
				continue;
			}
			UserInfoDao.getInstance().updateUserInfo(userInfo);
			size++;
		}
		return size;
	}
	
	/**
	 * 停服
	 */
	public static void stop() {
		int size = save();
		Log.info("停服执行完毕!save玩家size:"+size);
	}
	
	/**
	 * 获取该游戏最大的玩家id
	 * @param gameId
	 * @param needInit 是否需要初始化
	 * @return
	 */
	public synchronized static AtomicInteger getMaxUserId(int gameId,boolean needInit) {
		if(!gameUserMap.containsKey(gameId) && needInit) {
			String idStr = gameId+""+UserCenterMgr.INIT_USEER_ID;
			gameUserMap.put(gameId, new AtomicInteger(Integer.parseInt(idStr)));
		}
		return gameUserMap.get(gameId);
	}
	
	/**
	 * 初始化数据
	 * @return
	 */
	public static boolean init() {
		try {
			gameMap = GameInfoDao.getInstance().findGameInfos();
			gameUserMap = UserInfoDao.getInstance().findMaxUserId();
			serverMap = ServerInfoDao.getInstance().findServerInfos();
		}catch(Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * 根据游戏id获得服务器列表
	 * @param gameId
	 * @return
	 */
	public static List<ServerInfo> getServerList(int gameId){
		return serverMap.get(gameId);
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
