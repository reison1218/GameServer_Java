package com.recharge.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import com.recharge.base.db.HikariDBPool;

public class PayAppInfoDao {
	private static final PayAppInfoDao instance = new PayAppInfoDao();

	public static final PayAppInfoDao getInstance() {
		return instance;
	}

	/**
	 * 通过游戏id找
	 * @param gameId
	 * @return
	 */
	public PayAppInfo findByGameId(int gameId) {

		PayAppInfo po = new PayAppInfo();
		Connection conn = HikariDBPool.getDataConn();
		String sql = "select * from weixin_pay_app_info where game_id=" + gameId;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("id");
				int _gameId = rs.getInt("game_id");
				Date ctime = rs.getDate("ctime");
				String channelId = rs.getString("channel_id");
				String appSecret = rs.getString("app_secret");
				String appId = rs.getString("app_id");
				po.setId(id);
				po.setcTime(ctime);
				po.setGameId(_gameId);
				po.setChannelId(channelId);
				po.setAppId(appId);
				po.setAppSecret(appSecret);
				ps.close();
				rs.close();
				return po;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
