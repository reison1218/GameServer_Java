package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import game.base.db.HikariDBPool;

/**
 * 用户dao
 *
 * @author reison
 */
public class UserInfoDao {
    private static final UserInfoDao instance = new UserInfoDao();

    public static final UserInfoDao getInstance() {
        return instance;
    }

    /**
     * 新增用户
     */
    public int insertUserInfo(UserInfo userInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result = 0;
        try {
            String sql = "insert into t_users(user_id,register_platform,last_login_time,last_login_server) values(?,?,?,?)";
            ps = conn.prepareStatement(sql);
            Object[] oj = userInfo.toObjectArray();
            for (int i = 0; i < oj.length; i++) {
                ps.setObject(i + 1, oj[i]);
            }
            result = ps.executeUpdate();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
     */
    public int updateUserInfo(UserInfo userInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result = 0;
        try {
            String sql =
                    "update t_users set register_platform=?,platform_id=?,last_login_time=?,on_line=?,last_login_server=?  where user_id=? and game_id=?";
            ps = conn.prepareStatement(sql);
            Object[] oj = userInfo.toObjectArray();
            for (int i = 0; i < oj.length; i++) {
                ps.setObject(i + 1, oj[i]);
            }
            result = ps.executeUpdate();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 通过游戏id和玩家id找玩家数据
     */
    public UserInfo findUserByGameIdAndUserId(int _gameId, int _userId) {
        Connection conn = HikariDBPool.getDataConn();
        StringBuilder sb = new StringBuilder("select * from t_users");
        sb.append(" where game_id = ");
        sb.append("'" + _gameId + "'");
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
                String registerPlatform = rs.getString("register_platform");
                Date lastLoginTime = rs.getDate("last_login_time");
                String lastLoginServer = rs.getString("last_login_server");

                po.setUserId(userId);
                po.setLastLoginTime(lastLoginTime);
                po.setLoginServers(lastLoginServer);
                po.setRegisterPlatform(registerPlatform);
            }
            return po;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
     */
    public UserInfo findUserInfoByPlatformIdAndGameId(String _platformId, int _gameId) {
        Connection conn = HikariDBPool.getDataConn();
        StringBuilder sb = new StringBuilder("select * from t_users");
        sb.append(" where platform_id = ");
        sb.append("'" + _platformId + "'");
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
                String registerPlatform = rs.getString("register_platform");
                Date lastLoginTime = rs.getDate("last_login_time");
                String lastLoginServer = rs.getString("last_login_server");

                po.setUserId(userId);
                po.setLastLoginTime(lastLoginTime);
                po.setLoginServers(lastLoginServer);
                po.setRegisterPlatform(registerPlatform);
            }

            return po;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
