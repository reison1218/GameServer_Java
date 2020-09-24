package com.usercenter.entity.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.usercenter.base.db.HikariDBPool;

/**
 * 游戏配置dao
 * @author reison
 *
 */
public class GameConfigDao {
	
	private static GameConfigDao instance = new GameConfigDao();
	
	public static final GameConfigDao getInstance() {
		return instance;
	}
	
	/**
	 * 查找所有游戏
	 * @param gameId
	 * @return
	 */
	public Map<Integer, GameConfig> findGameConfigs() {
		Map<Integer, GameConfig> map = new ConcurrentHashMap<>();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from t_games";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int gameId = rs.getInt("game_id");
				int default_season = rs.getInt("default_season");
				GameConfig gi = new GameConfig();
				gi.setGame_id(gameId);
				gi.setDefault_season(default_season);
				map.put(gi.getGame_id(), gi);
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
