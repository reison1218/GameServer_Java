package com.usercenter.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.usercenter.base.db.HikariDBPool;

/**
 * 用户dao
 * @author reison
 *
 */
public class UserInfoDao {
	private static final UserInfoDao instance = new UserInfoDao();

	public static final UserInfoDao getInstance() {
		return instance;
	}
	
	/**
	 * 新增用户
	 * @param userInfo
	 * @return
	 */
	public int insertUserInfo(UserInfo userInfo) {
		Connection conn = HikariDBPool.getDataConn();
		PreparedStatement ps = null;
		int result = 0;
		try {
			String sql = "insert into t_users(nick_name,real_name,avatar,phone_no,register_ip,create_time,register_platform,platform_id,last_login_time,on_line,user_id,game_id) values(?,?,?,?,?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(sql);
			Object[] oj = userInfo.toObjectArray();
			for(int i = 0;i<oj.length;i++) {
				ps.setObject(i+1, oj[i]);
			}
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
	
	
	/**
	 * 新增用户
	 * @param userInfo
	 * @return
	 */
	public int updateUserInfo(UserInfo userInfo) {
		Connection conn = HikariDBPool.getDataConn();
		PreparedStatement ps = null;
		int result = 0;
		try {
			String sql = "update t_users set nick_name=?,real_name=?,avatar=?,phone_no=?,register_ip=?,create_time=?,register_platform=?,platform_id=?,last_login_time=?,on_line=? where user_id=? and game_id=?";
			ps = conn.prepareStatement(sql);
			Object[] oj = userInfo.toObjectArray();
			for(int i = 0;i<oj.length;i++) {
				ps.setObject(i+1, oj[i]);
			}
			
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
	
	/**
	 * 寻找游戏玩家最大的id
	 * @return
	 */
	public Map<Integer,AtomicInteger>  findMaxUserId(){
		Map<Integer,AtomicInteger> gameUserMap = new ConcurrentHashMap<Integer,AtomicInteger>();
		Connection conn = HikariDBPool.getDataConn();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select max(user_id) as user_id,game_id from t_users group by game_id";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			while (rs.next()) {
				int gameId = rs.getInt("game_id");
				AtomicInteger userId=new AtomicInteger(rs.getInt("user_id")); 
				gameUserMap.put(gameId, userId);
			}
			
			return gameUserMap;
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
	 * 通过游戏id和玩家id找玩家数据
	 * @param _gameId
	 * @param _userId
	 * @return
	 */
	public UserInfo findUserByGameIdAndUserId(int _gameId,int _userId) {
		Connection conn = HikariDBPool.getDataConn();
		StringBuilder sb = new StringBuilder("select * from t_users");
		sb.append(" where game_id = ");
		sb.append("'"+_gameId+"'");
		sb.append(" and user_id = ");
		sb.append(_userId);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = sb.toString();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			UserInfo po = null;
			while (rs.next()) {
				po = new UserInfo();
				int userId = rs.getInt("user_id");
				int gameId = rs.getInt("game_id");
				String nickName = rs.getString("nick_name");
				String realName = rs.getString("real_name");
				String avatar = rs.getString("avatar");
				String phoneNo = rs.getString("phone_no");
				String registerIp = rs.getString("register_ip");
				Date createTime = rs.getDate("create_time");
				String registerPlatform = rs.getString("register_platform");
				String platformId = rs.getString("platform_id");
				Date lastLoginTime = rs.getDate("last_login_time");
				boolean onLine = rs.getBoolean("on_line");
				po.setUser_id(userId);
				po.setGame_id(gameId);
				po.setNick_name(nickName);
				po.setReal_name(realName);
				po.setAvatar(avatar);
				po.setPhone_no(phoneNo);
				po.setRegister_ip(registerIp);
				po.setCreate_time(createTime);
				po.setRegister_platform(registerPlatform);
				po.setPlatform_id(platformId);
				po.setLast_login_time(lastLoginTime);
				po.setOn_line(onLine);
			}
			return po;
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
	 * 通过游戏id找
	 * @param gameId
	 * @return
	 */
	public UserInfo findUserInfoByPlatformIdAndGameId(String _platformId,int _gameId) {
		Connection conn = HikariDBPool.getDataConn();
		StringBuilder sb = new StringBuilder("select * from t_users");
		sb.append(" where platform_id = ");
		sb.append("'"+_platformId+"'");
		sb.append(" and game_id = ");
		sb.append(_gameId);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = sb.toString();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery(sql);
			UserInfo po = null;
			while (rs.next()) {
				po = new UserInfo();
				int userId = rs.getInt("user_id");
				int gameId = rs.getInt("game_id");
				String nickName = rs.getString("nick_name");
				String realName = rs.getString("real_name");
				String avatar = rs.getString("avatar");
				String phoneNo = rs.getString("phone_no");
				String registerIp = rs.getString("register_ip");
				Date createTime = rs.getDate("create_time");
				String registerPlatform = rs.getString("register_platform");
				String platformId = rs.getString("platform_id");
				Date lastLoginTime = rs.getDate("last_login_time");
				boolean onLine = rs.getBoolean("on_line");
				po.setUser_id(userId);
				po.setGame_id(gameId);
				po.setNick_name(nickName);
				po.setReal_name(realName);
				po.setAvatar(avatar);
				po.setPhone_no(phoneNo);
				po.setRegister_ip(registerIp);
				po.setCreate_time(createTime);
				po.setRegister_platform(registerPlatform);
				po.setPlatform_id(platformId);
				po.setLast_login_time(lastLoginTime);
				po.setOn_line(onLine);
			}
			
			return po;
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
