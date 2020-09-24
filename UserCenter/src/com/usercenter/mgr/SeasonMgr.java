package com.usercenter.mgr;

import java.util.Date;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.usercenter.entity.GameInfo;
import com.usercenter.entity.GameInfoDao;
import com.usercenter.entity.SeasonInfo;
import com.usercenter.entity.SeasonInfoDao;
import com.usercenter.entity.config.GameConfig;
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
			if(si.getSeasonId() == 0) {
				si.setSeasonId(gc.getDefault_season());
				si.setGameId(gc.getGame_id());
				si.setLasUpdateTime(new Date());
			}
		}
		return false;
	}
}
