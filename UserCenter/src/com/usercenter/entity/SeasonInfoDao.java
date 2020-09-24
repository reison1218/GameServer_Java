package com.usercenter.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.usercenter.base.db.HikariDBPool;

public class SeasonInfoDao {
	
	private static SeasonInfoDao instance = new SeasonInfoDao();
	
	public static SeasonInfoDao getInstance() {
		return instance;
	}
	
	/**
	 * 查找所有赛季数据
	 * @return
	 */
	public Map<Integer, SeasonInfo> findSeasonInfos() {
		Map<Integer, SeasonInfo> map = new ConcurrentHashMap<>();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from t_games";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int gameId = rs.getInt("game_id");
				int seasonId = rs.getInt("season_id");
				Date lastUpdateTime = rs.getDate("last_update_time");
				SeasonInfo si = new SeasonInfo();
				si.setGameId(gameId);
				si.setSeasonId(seasonId);
				si.setLasUpdateTime(lastUpdateTime);
				map.put(si.getGameId(), si);
			}
			
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				ps.close();
				rs.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	

}
