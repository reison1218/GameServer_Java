package com.usercenter.mgr;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.usercenter.entity.SeasonInfo;
import com.usercenter.entity.SeasonInfoDao;
import com.usercenter.entity.ServerInfo;
import com.usercenter.entity.config.GameConfig;
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
 * @author reison
 *
 */
public class SeasonMgr {

	private static SeasonMgr instance = new SeasonMgr();
	
	public static final SeasonMgr getInstance() {
		return instance;
	}
	
	/**赛季管理器**/
	private Map<Integer,SeasonInfo> seasonMap = new HashedMap<>();

	public boolean init() {
		seasonMap = SeasonInfoDao.getInstance().findSeasonInfos();
		Map<Integer,GameConfig> map = ConfigMgr.getInstance().getAllGameConfigs();
		for(GameConfig gc:map.values()) {
			if(gc.getDefault_season() == 0) {
				continue;
			}
			if(seasonMap.containsKey(gc.getGame_id())) {
				seasonMap.put(gc.getGame_id(), new SeasonInfo());
			}
			SeasonInfo si = seasonMap.get(gc.getGame_id());
			if(si.getSeason_id() == 0) {
				si.setSeason_id(gc.getDefault_season());
				si.setGame_id(gc.getGame_id());
				si.setLast_update_time(new Date());
			}
		}
		return false;
	}
	
	public void updateSeason() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day, 0, 0, 0);
		Date time = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		Date next_update_time = calendar.getTime();
		SeasonTemplateMgr stm = (SeasonTemplateMgr)TemplateMgr.getTemlateMgr(SeasonTemplateMgr.class);
		for(SeasonInfo si:seasonMap.values()) {
			int nextId = stm.getNextSeasonId(si.getGame_id(), si.getSeason_id());
			if(nextId == 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("gameId:");
				sb.append(si.getGame_id());
				sb.append(" do not has next seasonId!pls check!");
				Log.error(sb.toString());
				continue;
			}
			si.setLast_update_time(time);
			si.setSeason_id(nextId);	
			String jsonStr = JsonUtil.stringify(si);
			//持久化到redis
			RedisPool.hsetWithIndex(2, RedisKey.GAME_SEASON, String.valueOf(si.getGame_id()), jsonStr, 0);
			//持久化到数据库
			SeasonInfoDao.getInstance().updateSeasonInfo(si);
			//通知所有游戏服务器更新赛季信息
			List<ServerInfo> serverList = UserCenterMgr.getServerList(101);
			StringBuffer sb = null;
			Map<String,Object> map = new HashedMap<String, Object>();
			for(ServerInfo serverInfo : serverList) {
				sb = new StringBuffer();
				sb.append("http://");
				sb.append(serverInfo.getIp());
				sb.append("/");
				sb.append(serverInfo.getHttp_port());
				map.put("season_id", si.getSeason_id());
				map.put("last_update_time", si.getLast_update_time());
				map.put("next_update_time", TimeUtil.getDateFormat(next_update_time));
				String res = HttpUtil.doPost(sb.toString(), map, true);
				System.out.println(res);
			}
		}
	}
}
