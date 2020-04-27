/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.mgr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.base.key.PlayerKey;
import com.base.mgr.RankMgr;
import com.base.module.IModule;
import com.base.module.ModuleMgr;
import com.base.netty.LogInResult;
import com.base.netty.packet.Packet;
import com.base.type.ServerStatus;
import com.game.code.CodeMgr;
import com.game.code.impl.AnnReqCode;
import com.game.module.impl.HeatBallInventory;
import com.game.netty.handler.GameSocketServerHandler;
import com.game.player.GamePlayer;
import com.game.player.LoadStatus;
import com.utils.Log;
import com.utils.TimeUtil;

/**
 * <pre>
 * 游戏管理
 * </pre>
 * 
 * @author reison
 */
public class GameMgr {

	/** 连接断开多少秒卸载数据(后面有空再弄) */
	private final static int UNLOAD_DATA_OFF_SECS = 60 * 21;

	/** 在内存中的玩家 */
	protected final static Map<Integer, GamePlayer> memPlayers = new ConcurrentHashMap<>(1000);

	/** 正在加载中标志位，防止瞬间重复加载 */
	private final static Map<Integer, AtomicInteger> loadingFlag = new ConcurrentHashMap<>();

	/** 当前服务器状态 */
	public static short server_status = 0;

	/**
	 * <pre>
	 * 获取内存玩家
	 * </pre>
	 *
	 * @param userId
	 * @return
	 */
	public static GamePlayer getCachePlayer(int userId) {
		return memPlayers.get(userId);
	}

	/**
	 * <pre>
	 * 移除玩家
	 * </pre>
	 *
	 * @param userId
	 */
	public final static void removePlayer(int userId) {
		memPlayers.remove(userId);
		loadingFlag.remove(userId);
	}

	/**
	 * <pre>
	 * 加载玩家(先创建玩家队列)
	 * </pre>
	 *
	 * @param player
	 * @return
	 */
	public final static GamePlayer loadPlayer(GamePlayer player) {
		int userId = player.getUserId();
		if (!player.loadData()) {
			removeLoadStatus(userId);
			Log.error("用户数据加载失败，userId:" + userId);
			player = null;
			return null;
		}
		// 设置已加载完成
		memPlayers.put(userId, player);
		setLoadStatus(userId, LoadStatus.LOADED);
		return player;
	}

	/**
	 * <pre>
	 * 反射调用协议号逻辑
	 * </pre>
	 * 
	 * @param <T>
	 * @param userId
	 * @param code
	 * @return T
	 */
	public static <T> void invoke(GamePlayer player, Packet packet) {
		// 获取协议号的对应注解的类
		AnnReqCode annCode = CodeMgr.getCodeAnn(packet.getDesc().getCode());
		if (annCode == null) {
			Log.error("协议号缺失注解，code:" + packet.getDesc().getCode());
			return;
		}
		try {
			// 逻辑返回值
			T t = player.invoke(annCode.clazz(), annCode.methodName(), packet);
			if (t != null && !(t instanceof Boolean)) {
				Log.error("协议号逻辑返回值必须为布尔类型,result:" + t + ",code:" + packet.getDesc().getCode());
			}
		} catch (Throwable e) {
			Log.error("逻辑调用发生异常", e);
		} finally {
			packet.release();
			packet = null;
		}
	}

	/**
	 * <pre>
	 * 调用单例模块方法
	 * </pre>
	 *
	 * @param reqInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T invokeCmdModule(Packet packet) {
		
		GamePlayer player = getCachePlayer(packet.getUserId());
		if(player == null) {
			Log.error("player is null for userId:"+packet.getUserId());
			return null;
		}
				
		int code = packet.getCode();
		AnnReqCode annCode = CodeMgr.getCodeAnn(code);
		StringBuilder debug = new StringBuilder();
		debug.append(annCode.clazz().getSimpleName());
		debug.append(".");
		debug.append(annCode.methodName());
		debug.append("()");
		debug.append(",code:");
		debug.append(code);
		final String debugMsg = debug.toString();
		IModule module = ModuleMgr.getCmdModule(annCode.clazz());
		if (module == null) {
			Log.error("找不到该单例模块：" + debugMsg);
			return null;
		}
		try {
			Method mth = module.getClass().getMethod(annCode.methodName(), Packet.class);
			if (mth == null) {
				Log.error("找不到该方法：" + debugMsg);
				return null;
			}
			return (T) mth.invoke(module, packet);
		} catch (Exception e) {
			Log.error("单例模块调用出错：" + debugMsg, e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <pre>
	 * 获取所有玩家，包括已下线但尚未卸载的
	 * </pre>
	 *
	 * @return
	 */
	public final static Collection<GamePlayer> getAllPlayers() {
		Collection<GamePlayer> players = new ArrayList<>(memPlayers.values());
		return players;
	}

	/**
	 * <pre>
	 * 获取所有在线玩家
	 * </pre>
	 *
	 * @return
	 */
	public final static List<GamePlayer> getAllOnlinePlayers() {
		List<GamePlayer> players = new ArrayList<>(memPlayers.values());
		for (Iterator<GamePlayer> it = players.iterator(); it.hasNext();) {
			GamePlayer player = it.next();
			if (!player.isOnline()) {
				it.remove();
			}
		}
		return players;
	}

	/**
	 * <pre>
	 * 获取所有在线玩家Id集合
	 * </pre>
	 *
	 * @return
	 */
	public final static List<Integer> getAllOnlineUserIds() {
		List<Integer> userIds = new ArrayList<>();
		List<GamePlayer> players = new ArrayList<>(memPlayers.values());
		for (Iterator<GamePlayer> it = players.iterator(); it.hasNext();) {
			GamePlayer player = it.next();
			if (player.isOnline()) {
				userIds.add(player.getUserId());
			}
		}
		return userIds;
	}

	/**
	 * <pre>
	 * 获取在线玩家数量
	 * </pre>
	 *
	 * @return
	 */
	public final static int getOnlineCount() {
		int count = 0;
		List<GamePlayer> players = new ArrayList<>(memPlayers.values());
		for (Iterator<GamePlayer> it = players.iterator(); it.hasNext();) {
			GamePlayer player = it.next();
			if (player.isOnline()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * <pre>
	 * 获取所有离线但尚未卸载的玩家
	 * </pre>
	 *
	 * @return
	 */
	public final static List<GamePlayer> getAllOfflinePlayers() {
		List<GamePlayer> players = new ArrayList<>(memPlayers.values());
		for (Iterator<GamePlayer> it = players.iterator(); it.hasNext();) {
			GamePlayer player = it.next();
			if (player.isOnline()) {
				it.remove();
			}
		}
		return players;
	}

	/**
	 * <pre>
	 * 保存数据
	 * </pre>
	 */
	public synchronized static void save() {
		Collection<GamePlayer> allPlayers = getAllPlayers();
		for (GamePlayer player : allPlayers) {
			try {
				player.save();
			} catch (Throwable e) {
				Log.error("保存玩家数据异常", e);
			}
		}
	}

	/**
	 * <pre>
	 * 每日0点重置玩家数据
	 * </pre>
	 */
	public final synchronized static void dayReset() {
		Collection<GamePlayer> allPlayers = getAllPlayers();
		for (GamePlayer player : allPlayers) {
			try {
				long t1 = System.currentTimeMillis();
				player.dayReset();
				long ct = System.currentTimeMillis() - t1;
				if (ct > 20) {
					Log.fatal("Player_dayReset_costTime:" + ct + "ms,userId:" + player.getUserId());
				}
			} catch (Throwable e) {
				Log.error("重置玩家数据异常:" + player, e);
			} finally {
				Log.info("CachePlayerDayReset:" + player);
			}
		}
		// 重置排行榜
		RankMgr.resetRank();
	}

	/**
	 * <pre>
	 * 卸载所有玩家
	 * </pre>
	 */
	public final synchronized static void unloadAll() {
		Collection<GamePlayer> allPlayers = getAllPlayers();
		for (GamePlayer player : allPlayers) {
			try {
				unloadPlayer(player, LogInResult.STOP);
			} catch (Throwable e) {
				Log.error("卸载玩家数据异常", e);
			}
		}
	}

	/**
	 * <pre>
	 * 卸载玩家数据
	 * </pre>
	 *
	 * @param player
	 * @return
	 */
	public final static boolean unloadPlayer(GamePlayer player, short code) {
		logoffPlayer(player.getUserId());
		player.unloadData();
		GameSocketServerHandler.sendGateRes(code, player);
		return true;
	}

	public static void logoffPlayer(int userId) {
		logoffPlayer(userId, true);
	}

	/**
	 * 处理玩家下线
	 * 
	 * @param userId
	 * @param save
	 */
	public static void logoffPlayer(int userId, boolean save) {
		GamePlayer player = null;
		try {
			player = GameMgr.getCachePlayer(userId);
			if (player == null) {
				Log.error("处理玩家下线错误，内存找不到该玩家,userId:" + userId);
				return;
			}
			// 更新下线时间
			long time = System.currentTimeMillis();
			player.setOffLineSecs(time);

			// 更新在线时长
			player.addData(PlayerKey.TOTLA_ONLINE_TIME, (int) (time / 1000) - player.getLastLoginTimeSecs());
			// 当前秒数时间
			Log.info("玩家下线：" + player + ",time is:" + time);
		} catch (Exception e) {
			Log.error("处理玩家下线异常", e);
		} finally {
			// 保存数据
			if (player != null) {
				Log.info("GameMgrLogoffPlayer：" + player);
				if (save) {
					// 计算玩家星球收益
					HeatBallInventory si = player.getModule(HeatBallInventory.class);
					si.calcIncome(player);
					player.save();
				}
				// 标记玩家下线状态
				player.setData(PlayerKey.USER_OL, 0);
			}
		}
	}

	/**
	 * <pre>
	 * 发送广播数据包信息
	 * </pre>
	 *
	 * @param code
	 * @param content
	 * @return
	 */
	public static final boolean sendBroadMessage(short code, Object content) {
		// CommonExecutor broad = ExecutorMgr.getBroadExecutor();
		// broad.enQueue(new BroadAction(broad.getActionQueue(), code,
		// getAllOnlineUserIds(), content));
		return true;
	}

	/**
	 * 保存玩家数据
	 */
	public static final void stopSave() {
		Log.info("执行退出进程保存逻辑");
		long start = System.currentTimeMillis();
		save();
		long end = System.currentTimeMillis();
		Log.info("执行退出进程保存完成，耗时:" + (end - start) + "ms");
	}

	/**
	 * <pre>
	 * 检测下线玩家已离线时间，并卸载数据
	 * </pre>
	 */
	public final static void checkUnloadData() {
		Collection<GamePlayer> offPlayers = getAllOfflinePlayers();
		for (GamePlayer player : offPlayers) {
			int offSecs = TimeUtil.getCurSecs() - player.getOffLineSecs();
			// 高级别玩家，下线超过n分钟卸载数据
			boolean supriorPlayerOff = player.getOffLineSecs() > 0 && offSecs > UNLOAD_DATA_OFF_SECS;
			// 新手玩家，下线超过定时器周期即卸载数据，新区导量较快，防止内存飙升
			boolean newPlayerOff = player.getOffLineSecs() > 0 && offSecs > 60;
			if (supriorPlayerOff || newPlayerOff) {
				player.unloadData();
			}
		}
	}

	/**
	 * 获取所有在线玩家
	 * 
	 * @return
	 */
	public static final Map<Integer, GamePlayer> getMemPlayers() {
		return memPlayers;
	}

	/**
	 * <pre>
	 * 服务器是否处于运行状态
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean checkRunning() {
		return server_status == ServerStatus.RUNNING;
	}

	/**
	 * <pre>
	 * 检测是否正在加载中，并初始化为加载状态
	 * </pre>
	 *
	 * @param userId
	 * @return false不在加载中 true正在加载中
	 */
	public final static boolean checkInitLoading(int userId) {
		AtomicInteger flag = loadingFlag.get(userId);
		if (flag == null) {
			flag = new AtomicInteger();
			loadingFlag.put(userId, flag);
		}
		if (flag.compareAndSet(LoadStatus.NOT_LOAD, LoadStatus.LOADING)
				|| flag.compareAndSet(LoadStatus.LOADED, LoadStatus.LOADING)) {
			return false;
		}
		return true;
	}

	/**
	 * <pre>
	 * 检测是否正在加载中
	 * </pre>
	 *
	 * @param userId
	 * @return false不在加载中 true正在加载中
	 */
	public final static boolean checkLoading(int userId) {
		AtomicInteger flag = loadingFlag.get(userId);
		if (flag != null && flag.getAndAdd(0) == LoadStatus.LOADING) {
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 设置加载状态
	 * </pre>
	 *
	 * @param userId
	 */
	public final static void setLoadStatus(int userId, int status) {
		AtomicInteger flag = loadingFlag.get(userId);
		if (flag != null) {
			flag.getAndSet(status);
		}
	}

	/**
	 * <pre>
	 * 移除加载状态
	 * </pre>
	 *
	 * @param userId
	 */
	public final static void removeLoadStatus(int userId) {
		loadingFlag.remove(userId);
	}

}