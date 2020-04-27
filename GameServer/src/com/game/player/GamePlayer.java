/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.constant.ResConstants;
import com.base.dao.SQLExecutor;
import com.base.dbpool.HikariDBPool;
import com.base.key.BuffKey;
import com.base.key.HeatBallKey;
import com.base.key.PlayerKey;
import com.base.mgr.RankMgr;
import com.base.mgr.RankType;
import com.base.module.IModule;
import com.base.module.JsonDataModule;
import com.base.module.able.Loadable;
import com.base.module.able.Resetable;
import com.base.module.able.Savable;
import com.base.module.able.Sendable;
import com.base.module.able.Unloadable;
import com.base.redis.RedisKey;
import com.base.redis.RedisPool;
import com.base.template.TurnTableTemplate;
import com.base.template.mgr.ConstantTemplateMgr;
import com.base.template.mgr.TemplateMgr;
import com.base.template.mgr.TurnTableTemplateMgr;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.module.impl.BuffInventory;
import com.game.module.impl.FragmentInventory;
import com.game.module.impl.HeatBallInventory;
import com.game.module.impl.SignInInventory;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import proto.BaseProto;
import proto.BaseProto.BuffPt;
import proto.BaseProto.HeatBallBuyTimesPt;
import proto.BaseProto.HeatBallPt;
import proto.BaseProto.ResourcesPt;
import proto.UserProto;

/**
 * <pre>
 * 玩家对象
 * </pre>
 * 
 * @author reison
 */
public class GamePlayer extends AbstractPlayer {

	/** 玩家能升到的最高等级 */
	public final static int MAX_GRADE = 998;

	public GamePlayer(int userId) {
		super(userId);
	}

	/**
	 * <pre>
	 * 初始化基础数据
	 * </pre>
	 * 
	 */
	@Override
	public void initData() {

		ConstantTemplateMgr mgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);

		// 初始化启动金币资金
		if (getDoubleData(PlayerKey.GOLD) == 0) {
			double gold = mgr.getDoubleValue("newPlayer.gold");
			setData(PlayerKey.GOLD, gold);
		}

		if (getDoubleData(PlayerKey.DIAMONDS) == 0) {
			double diamond = mgr.getDoubleValue("newPlayer.diamond");
			setData(PlayerKey.DIAMONDS, diamond);
		}

		if (getDoubleData(PlayerKey.TURN_COUNT) == 0) {
			setData(PlayerKey.TURN_COUNT, 0);
		}
	}

	/**
	 * <pre>
	 * 重置数据
	 * </pre>
	 */
	public final void dayReset() {
		setData(PlayerKey.TURN_COUNT, 0);
		// 记录模块重置时间
		List<Map<String, Object>> costTimeList = new ArrayList<>();
		// 重置各模块数据
		Collection<IModule> modules = moduleMap.values();
		for (IModule module : modules) {
			if (!(module instanceof Resetable)) {
				continue;
			}
			try {
				long t1 = System.currentTimeMillis();
				((Resetable) module).dayReset();
				if (module instanceof Sendable) {
					((Sendable) module).sendSome();
				}
				long costTime = System.currentTimeMillis() - t1;
				Map<String, Object> costTimeMap = new HashMap<>();
				costTimeMap.put("name", module.getClass().getSimpleName());
				costTimeMap.put("time", costTime);
				costTimeList.add(costTimeMap);
			} catch (Throwable e) {
				Log.error("重置模块数据异常,player:" + this.toString() + ",module:" + module.getClass().getSimpleName(), e);
			}
		}
		// 按耗时打印重置时间
		printResetTime(costTimeList);
		// 更新重置时间
		setData(PlayerKey.RESET_TIME, new Date());
	}

	/**
	 * <pre>
	 * 打印重置时间
	 * </pre>
	 */
	public final void printResetTime(List<Map<String, Object>> costTimeList) {
		Collections.sort(costTimeList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				long l1 = Long.parseLong(o1.get("time").toString());
				long l2 = Long.parseLong(o2.get("time").toString());
				return l1 > l2 ? -1 : l1 == l2 ? 0 : 1;
			}
		});
		StringBuilder logSb = new StringBuilder("[");
		for (int i = 0; i < costTimeList.size(); i++) {
			Map<String, Object> objmap = costTimeList.get(i);
			String moduleName = objmap.get("name").toString();
			String costTime = objmap.get("time").toString();
			logSb.append(moduleName).append(": ").append(costTime);
			if (i < 4) {
				logSb.append("ms, ");
			} else {
				logSb.append("ms]");
				break;
			}
		}
		Log.info("Player_dayReset_costTime:" + logSb.toString() + ",userId:" + userId);
	}

	/**
	 * 发送玩家数据
	 */
	@SuppressWarnings("unchecked")
	public final void send() {
		// 加载完成后的逻辑(防止因为模块依赖报错)
		afterLoad();
		// 玩家基础数据
		UserProto.LoginRsp.Builder builder = UserProto.LoginRsp.newBuilder();
		builder.setIsSucc(true);
		builder.setUserId(getUserId());
		builder.setAvatar(getStringData(PlayerKey.AVATAR));
		builder.setNickname(getNickNameWithSite());
		builder.setLastLoginTime(getLastLoginTimeSecs());
		SignInInventory sii = getModule(SignInInventory.class);
		HeatBallInventory si = getModule(HeatBallInventory.class);
		builder.setLastLogOffTime(getOffLineSecs());
		int battlePos = si.getBattlePos();
		builder.setBattlePosSlime(battlePos);
		builder.setOffLineGold(getDoubleData(PlayerKey.OFF_LINE_GOLD));
		builder.setSignIn(sii.getDays());
		builder.setSignInTime(sii.getTime());
		String tId = RedisPool.hget(RedisKey.TURN_TABLE, Integer.toString(getUserId()));
		if (!StringUtils.isEmpty(tId)) {
			TurnTableTemplateMgr tMgr = (TurnTableTemplateMgr) TemplateMgr.getTemlateMgr(TurnTableTemplateMgr.class);
			TurnTableTemplate ttTemplate = tMgr.getById(Integer.parseInt(tId));
			for (ResourcesPt.Builder b : ttTemplate.getRewardList()) {
				builder.addTurnCountReward(b);
			}
		}

		// 资源
		for (int i = ResConstants.GOLD; i <= ResConstants.TURN_COUNT; i++) {
			double value = getDoubleData(ResConstants.getPlayerKey(i));
			ResourcesPt.Builder _builder = ResourcesPt.newBuilder();
			_builder.setId(i);
			_builder.setNum(value);
			builder.addResp(_builder);
		}

		// 星球
		JSONArray heatBall = si.getHeatBallData();
		HeatBallPt.Builder sBuilder = null;
		for (int i = 0; i < heatBall.size(); i++) {
			sBuilder = HeatBallPt.newBuilder();
			sBuilder.setIndex(i);
			sBuilder.setHeatBallId(heatBall.getIntValue(i));
			builder.addSp(sBuilder);
		}
		// buff
		BuffInventory bi = getModule(BuffInventory.class);
		if (!bi.getAllData().isEmpty()) {
			Date start = null;
			Date end = null;
			Set<Entry<Integer, JSONObject>> set = bi.getAllData().entrySet();
			for (Entry<Integer, JSONObject> en : set) {
				BuffPt.Builder bBulder = BuffPt.newBuilder();
				bBulder.setBuffCategory(en.getKey());
				start = en.getValue().getDate(BuffKey.START_TIME);
				end = en.getValue().getDate(BuffKey.END_TIME);
				bBulder.setBuffStartTime(start == null ? 0 : (int) (start.getTime() / 1000));
				bBulder.setBuffEndTime(end == null ? 0 : (int) (end.getTime() / 1000));
				builder.addBuff(bBulder);
			}
		}
		// 封装购买次数
		Map<Integer, Integer> map = si.getData(HeatBallKey.BUY_TIMES);
		if (map != null && !map.isEmpty()) {
			for (Integer heatBallId : map.keySet()) {
				HeatBallBuyTimesPt.Builder btBuilder = HeatBallBuyTimesPt.newBuilder();
				btBuilder.setHeatBallId(heatBallId);
				btBuilder.setBuyTimes(map.get(heatBallId));
			}
		}
		// 发送数据
		this.sendGatePacket(GateServerCode.LOGIN_IN, builder.build().toByteArray());
		printSendBytes();
	}

	/**
	 * 添加资源
	 * 
	 * @param id
	 * @param num
	 */
	public final double addRes(int type, int id, double num, boolean needSend) {

		double result = 0;

		// 判断是否物品
		if (type == ResConstants.GOODS) {
			FragmentInventory fi = this.getModule(FragmentInventory.class);
			result = fi.addData(Integer.toString(id), (int) num);
		} else if (type == ResConstants.RES) {// 判断是否资源
			String key = null;
			if (id == ResConstants.GOLD) {
				key = PlayerKey.GOLD;
			} else if (id == ResConstants.DIAMONDS) {
				key = PlayerKey.DIAMONDS;
			} else if (id == ResConstants.TURN_COUNT) {
				key = PlayerKey.TURN_COUNT;
			}
			if (key == null)
				return -1;
			Number n = addDoubleData(key, num);
			if (!needSend) {
				return n.doubleValue();
			}
			result = n.doubleValue();
		}

		// 封装发给客户端
		UserProto.ResoucesRsp.Builder builder = UserProto.ResoucesRsp.newBuilder();
		BaseProto.ResourcesPt.Builder _builder = BaseProto.ResourcesPt.newBuilder();
		_builder.setType(type);
		_builder.setId(id);
		_builder.setNum(result);
		builder.addResp(_builder);
		builder.setSysTime(TimeUtil.getCurSecs());
		sendClientPacket(GateServerCode.RESOURCES, builder.build().toByteArray());
		return result;
	}

	/**
	 * 批量添加资源
	 * 
	 * @param list
	 * @param needSend
	 */
	public final void addRes(List<ResourcesPt.Builder> list, boolean needSend) {
		if (list == null || list.isEmpty())
			return;
		UserProto.ResoucesRsp.Builder builder = null;
		if (needSend) {
			builder = UserProto.ResoucesRsp.newBuilder();
			builder.setSysTime(TimeUtil.getCurSecs());
		}
		for (ResourcesPt.Builder b : list) {
			double result = addRes(b.getType(), b.getId(), b.getNum(), false);
			if (!needSend || result < 0)
				continue;
			ResourcesPt.Builder rsp = ResourcesPt.newBuilder();
			rsp.setId(b.getId());
			rsp.setNum(getDoubleData(ResConstants.getPlayerKey(b.getId())));
			builder.addResp(rsp);
		}
		if (!needSend)
			return;
		// 发送客户端
		sendClientPacket(GateServerCode.RESOURCES, builder.build().toByteArray());
	}

	/**
	 * 检查离线收益金币
	 * 
	 * @return
	 */
	public boolean checkOffLineGold() {
		double gold = getDoubleData(PlayerKey.OFF_LINE_GOLD);
		return gold == 0 ? true : false;
	}

	/**
	 * <pre>
	 * 加载数据
	 * ！！！切记！！！在各模块加载中不做逻辑处理
	 * ！！！否则可能会有模块加载先后顺序问题
	 * </pre>
	 *
	 * @return
	 */
	public synchronized boolean loadData() {
		try {
			// 检测数据库是否正常，防止t_u_player表数据被重新初始化
			if (!HikariDBPool.checkDB()) {
				return false;
			}
			// 加载玩家基本数据
			String selectSql = String.format(JsonDataModule.SELECT_SQL, moduleName, getUserId());
			Map<String, Object> tempMap = SQLExecutor.execSelectJSON(selectSql);
			// 新玩家，需新建数据
			if (tempMap == null) {
				tempMap = new HashMap<>();
				String insertSql = String.format(JsonDataModule.INSERT_SQL, moduleName);
				SQLExecutor.execInsertReturnId(insertSql, getUserId(), "{}");
			}

			getDataMap().putAll(tempMap);
			tempMap.clear();
			tempMap = null;
			// 加载各模块数据
			Collection<IModule> modules = moduleMap.values();
			for (IModule module : modules) {
				if (!(module instanceof Loadable)) {
					Log.error("发现模块没有使用通用加载:" + module.getClass().getSimpleName());
					continue;
				}
				try {
					((Loadable) module).load();
				} catch (Throwable e) {
					Log.error("加载模块数据异常,player:" + this.toString() + ",module:" + module.getClass().getSimpleName(), e);
				}
			}
			return true;
		} catch (Exception e) {
			Log.error("加载用户数据异常 " + this, e);
			unloadData();
		}
		return false;
	}

	/**
	 * <pre>
	 * 加载完成后的逻辑
	 * </pre>
	 */
	public final void afterLoad() {
		// 初始化数据
		initData();
		// 各模块加载后逻辑
		Collection<IModule> modules = moduleMap.values();
		// 重置时间
		for (IModule module : modules) {
			try {
				if (module instanceof Loadable) {
					((Loadable) module).afterLoad();
				}
			} catch (Throwable e) {
				Log.error("模块加载完成后的逻辑异常,player:" + this.toString() + ",module:" + module.getClass().getSimpleName(), e);
			}
		}
	}

	/**
	 * <pre>
	 * 卸载数据
	 * !内存保留一定时间的用户数据
	 * </pre>
	 *
	 * @return
	 */
	public final synchronized boolean unloadData() {
		try {
			Log.info("卸载玩家数据 " + this);
			save();
			getDataMap().clear();
			// 卸载各模块数据
			Collection<IModule> modules = moduleMap.values();
			for (IModule module : modules) {
				if (!(module instanceof Unloadable)) {
					Log.error("发现模块没有使用通用卸载:" + module.getClass().getSimpleName());
					continue;
				}
				try {
					((Unloadable) module).unload();
				} catch (Throwable e) {
					Log.error("卸载模块数据异常,player:" + this.toString() + ",module:" + module.getClass().getSimpleName(), e);
				}
			}
			moduleMap.clear();
			return true;
		} catch (Exception e) {
			Log.error("卸载用户数据异常 " + this, e);
		} finally {
			GameMgr.removePlayer(userId);
			GameMgr.removeLoadStatus(userId);
		}
		return true;
	}

	/**
	 * <pre>
	 * 保存数据
	 * </pre>
	 */
	public synchronized void save() {
		// 正在加载数据，不执行保存
		if (GameMgr.checkLoading(userId)) {
			return;
		}
		// 保存dataMap
		if (!getDataMap().isEmpty() && update.compareAndSet(true, false)) {
			String jsonStr = JsonUtil.stringify(getDataMap());
			System.out.println(jsonStr);
			String updateSql = String.format(JsonDataModule.UPDATE_SQL, moduleName);
			if (!SQLExecutor.execUpdate(updateSql, jsonStr, getUserId())) {
				update.getAndSet(true);
			}
		}
		// 保存各模块数据
		Collection<IModule> modules = new ArrayList<>(moduleMap.values());
		for (IModule module : modules) {
			if (!(module instanceof Savable)) {
				continue;
			}
			try {
				((Savable) module).save();
			} catch (Throwable e) {
				Log.error("保存模块数据异常,player:" + this.toString() + ",module:" + module.getClass().getSimpleName(), e);
			}
		}
	}

	/**
	 * 更新玩家记录
	 * 
	 * @param score
	 * @param jump
	 * @param jumpLevel
	 * @param cp
	 * @param cpMultiple
	 */
	public final void updateRecord(int type, int score, int jump, int jumpLevel, int cp, int cpMultiple) {

		String redisKey = null;
		switch (type) {
		case RankType.RANK_DAY:
			redisKey = RedisKey.RANK_DAY_KEY;
			break;

		case RankType.RANK_WEEK:
			redisKey = RedisKey.RANK_WEEK_KEY;
			break;
		}
		int maxCp = getData(PlayerKey.MAX_CP);
		if (maxCp < cp) {
			setData(PlayerKey.MAX_CP, cp);

			// 更新排行榜
			RankMgr.updatePlayerRank(this, redisKey, cp);
		}
		int pScore = getData(PlayerKey.MAX_SCORE);
		if (pScore < score) {
			setData(PlayerKey.MAX_SCORE, score);
		}

		int pJumpLevel = getData(PlayerKey.MAX_JUMP_LEVEL);
		if (pJumpLevel < jumpLevel) {
			setData(PlayerKey.MAX_SCORE, jumpLevel);
		}

		int pJumpRange = getData(PlayerKey.MAX_JUMP_RANGE);
		if (pJumpRange < jump) {
			setData(PlayerKey.MAX_JUMP_RANGE, jump);
		}

		int pMultiple = getData(PlayerKey.MAX_MULTIPLE);
		if (pMultiple < cpMultiple) {
			setData(PlayerKey.MAX_MULTIPLE, cpMultiple);
		}

	}

	/** 获取上次登陆时间 */
	public final int getLastLoginTimeSecs() {
		Date date = getData(PlayerKey.LAST_LOGIN_TIME);
		if (date == null)
			return 0;
		return (int) (date.getTime() / 1000);
	}

	/** 获取上次下线时间 */
	public final int getOffLineSecs() {
		Date date = getData(PlayerKey.OFF_TIME);
		if (date == null)
			return 0;
		return (int) (date.getTime() / 1000);
	}

	/** 设置本次下线时间 */
	public final void setOffLineSecs(long offLineSecs) {
		setData(PlayerKey.OFF_TIME, new Date(offLineSecs));
	}

	/** 获取上次下线时间到登录的时间 (秒) */
	public final int getOffLineTime() {
		return TimeUtil.getCurSecs() - getOffLineSecs();
	}
}