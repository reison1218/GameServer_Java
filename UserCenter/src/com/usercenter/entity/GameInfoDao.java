package com.usercenter.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.usercenter.base.db.HikariDBPool;


/**
 * 游戏dao
 * @author reison
 *
 */
public class GameInfoDao {
	
	private static final GameInfoDao instance = new GameInfoDao();

	public static final GameInfoDao getInstance() {
		return instance;
	}
	
	/**
	 * 查找所有游戏
	 * @param gameId
	 * @return
	 */
	public Map<Integer, GameInfo> findGameInfos() {
		Map<Integer, GameInfo> map = new ConcurrentHashMap<>();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from t_games";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int gameId = rs.getInt("game_id");
				String name = rs.getString("name");
				Date createTime = rs.getDate("create_time");
				Date releaseTime = rs.getDate("release_time");
				Date updateTime = rs.getDate("update_time");
				int defaultSeason = rs.getInt("default_season");
				String centerHttp = rs.getString("center_http"); 
				GameInfo gi = new GameInfo();
				gi.setGame_id(gameId);
				gi.setName(name);
				gi.setCreate_time(createTime);
				gi.setRelease_time(releaseTime);
				gi.setUpdate_time(updateTime);
				gi.setDefault_season(defaultSeason);
				gi.setCenter_http(centerHttp);
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
