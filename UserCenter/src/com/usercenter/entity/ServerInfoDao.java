package com.usercenter.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.usercenter.base.db.HikariDBPool;

public class ServerInfoDao {

	
	private static final ServerInfoDao instance = new ServerInfoDao();

	public static final ServerInfoDao getInstance() {
		return instance;
	}
	
	/**
	 * 查找所有游戏
	 * @param gameId
	 * @return
	 */
	public Map<Integer, List<ServerInfo>> findServerInfos() {
		
		Map<Integer, List<ServerInfo>> map = new ConcurrentHashMap<>();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from t_servers";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			List<ServerInfo> list = null;
			while (rs.next()) {
				int id = rs.getInt("id");
				int gameId = rs.getInt("game_id");
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				ServerInfo gi = new ServerInfo();
				gi.setId(id);
				gi.setGame_id(gameId);
				gi.setName(name);
				gi.setIp(ip);
				gi.setPort(port);
				if (!map.containsKey(gameId)) {
					map.put(gameId, new ArrayList<ServerInfo>());
				}
				list = map.get(gameId);
				list.add(gi);
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
