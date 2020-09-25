package com.usercenter.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.usercenter.base.db.HikariDBPool;
import com.utils.TimeUtil;

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
				si.setGame_id(gameId);
				si.setSeason_id(seasonId);
				si.setLast_update_time(lastUpdateTime);
				map.put(si.getGame_id(), si);
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
	
	/**
	 * 新增用户
	 * @param userInfo
	 * @return
	 */
	public int updateSeasonInfo(SeasonInfo seasonInfo) {
		Connection conn = HikariDBPool.getDataConn();
		PreparedStatement ps = null;
		int result = 0;
		try {
			String sql = "update t_season set season_id=?,last_update_time=?  where game_id=?";
			ps = conn.prepareStatement(sql);
			ps.setObject(0, seasonInfo.getSeason_id());
			ps.setObject(1, seasonInfo.getLast_update_time());
			ps.setObject(2, seasonInfo.getGame_id());
			result = ps.executeUpdate();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				ps.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
}
