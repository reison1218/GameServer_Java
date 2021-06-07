package com.usercenter.mgr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.usercenter.entity.GameInfo;
import com.usercenter.entity.SeasonInfo;
import com.usercenter.entity.SeasonInfoDao;
import com.usercenter.redis.RedisIndex;
import com.usercenter.redis.RedisKey;
import com.usercenter.redis.RedisPool;
import com.usercenter.template.mgr.SeasonTemplateMgr;
import com.usercenter.template.mgr.TemplateMgr;
import com.utils.HttpUtil;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.TimeUtil;

/**
 * 赛季管理器
 * 
 * @author reison
 *
 */
public class SeasonMgr {

	/** 赛季管理器 **/
	private static Map<Integer, SeasonInfo> seasonMap = new HashedMap<>();

	/**
	 * 初始化函数
	 * 
	 * @return
	 */
	public static boolean init() {
		seasonMap = SeasonInfoDao.getInstance().findSeasonInfos();
		Map<Integer, GameInfo> map = UserCenterMgr.getGameInfos();
		long nowTime = System.currentTimeMillis();
		boolean isInsert = false;
		Date lastTime = null;
		Date nextTime = null;
		boolean isContinue = false;
		boolean update = false;
		for (GameInfo gi : map.values()) {
			int gameId = gi.getGame_id();
			int defaultSeason = gi.getDefault_season();
			isContinue = false;
			update = false;
			if (defaultSeason == 0) {
				continue;
			}
			SeasonInfo si = seasonMap.get(gameId);
			if (si == null) {
				isInsert = true;

			} else {
				lastTime = TimeUtil.format(si.getLast_update_time_str());
				nextTime = TimeUtil.format(si.getNext_update_time_str());
				if (nowTime < lastTime.getTime() || nowTime > nextTime.getTime()) {
					isInsert = false;
					update = true;
				} else {
					isContinue = true;
				}
			}
			if (isContinue) {
				continue;
			}

			if (isInsert) {
				si = new SeasonInfo();
				seasonMap.put(gameId, si);
			}
			si = seasonMap.get(gameId);
			if (si.getSeason_id() == 0) {
				si.setSeason_id(defaultSeason);
				si.setGame_id(gameId);
			} else {
				SeasonTemplateMgr stMgr = (SeasonTemplateMgr) TemplateMgr.getTemlateMgr(SeasonTemplateMgr.class);
				int seasonId = stMgr.getNextSeasonId(si.getSeason_id());
				si.setSeason_id(seasonId);
			}

			si.setLast_update_time(new Date());
			si.setNext_update_time(getNextUpdateTime());
			//新增或者更新轮次自增1
			if(isInsert||update) {
				si.addRound();
			}
			// 更新到redis
			RedisPool.hsetWithIndex(RedisIndex.GAME_SEASON, RedisKey.GAME_SEASON, String.valueOf(si.getGame_id()),
					JsonUtil.stringify(si), 0);
			// 持久化到数据库
			if (isInsert) {
				SeasonInfoDao.getInstance().insertSeasonInfo(si);
			} else if (update) {
				SeasonInfoDao.getInstance().updateSeasonInfo(si);
			}
		}
		return true;
	}

	/**
	 * 更新赛季
	 */
	public static void updateSeason() {
		Date now = new Date();
		Date next_update_time = getNextUpdateTime();
		SeasonTemplateMgr stm = (SeasonTemplateMgr) TemplateMgr.getTemlateMgr(SeasonTemplateMgr.class);
		StringBuffer sb;
		Map<String, Object> map = new HashMap<>();
		for (SeasonInfo si : seasonMap.values()) {
			int nextId = stm.getNextSeasonId(si.getSeason_id());
			int gameId = si.getGame_id();
			if (nextId == 0) {
				sb = new StringBuffer();
				sb.append("gameId:");
				sb.append(si.getGame_id());
				sb.append(" do not has next seasonId!pls check!");
				Log.error(sb.toString());
				continue;
			}
			si.setLast_update_time(now);
			si.setNext_update_time(next_update_time);
			si.setSeason_id(nextId);
			si.addRound();
			String jsonStr = JsonUtil.stringify(si);
			// 持久化到redis
			RedisPool.hsetWithIndex(RedisIndex.GAME_SEASON, RedisKey.GAME_SEASON, String.valueOf(si.getGame_id()),
					jsonStr, 0);
			// 持久化到数据库
			SeasonInfoDao.getInstance().updateSeasonInfo(si);
			// 通知所有游戏服务器更新赛季信息
			GameInfo gi = UserCenterMgr.getGameInfo(gameId);
			if (gi == null) {
				Log.warn("could not find GameInfo,gameId:" + gameId);
				continue;
			}

			sb = new StringBuffer();
			sb.append(gi.getCenter_http());
			map.put("season_id", si.getSeason_id());
			map.put("round", si.getRound());
			map.put("next_update_time", next_update_time.getTime());
			String res = HttpUtil.doPost(sb.toString(), map, true);
			System.out.println(res);
		}
	}

	public static Date getNextUpdateTime() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int week_day = calendar.get(Calendar.DAY_OF_WEEK);
		int addDay = 7 - week_day + 2;
		calendar.add(Calendar.DAY_OF_WEEK, addDay);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day, 0, 0, 0);
		return calendar.getTime();
	}
}
