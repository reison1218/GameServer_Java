package game.entity;

import com.alibaba.fastjson.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import game.base.db.HikariDBPool;

/**
 * 用户dao
 *
 * @author reison
 */
public class UserInfoDao {
    private static final UserInfoDao instance = new UserInfoDao();

    public static UserInfoDao getInstance() {
        return instance;
    }

    /**
     * 新增用户
     */
    public int insertUserInfo(UserInfo userInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "replace into users(name,combine_id,operator_id,server_id,player_name,login_time) values(?,?,?,?,?,?)";
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
     * 更新用户
     */
    public int updateUserInfo(UserInfo userInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result = 0;
        try {
            String sql = "update users set player_name = ?,login_time=?  where combine_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, userInfo.getPlayerName());
            ps.setObject(2, userInfo.getLastLoginTime());
            ps.setObject(3, userInfo.getCombineId());
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

    public JSONObject findUsersLoginInfo(String accountName) {

        JSONObject json = new JSONObject();
        Connection conn = HikariDBPool.getDataConn();
        String sql = "select server_id,login_time from `users` where `name` ='"+accountName+"'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                int serverId = rs.getInt("server_id");
                long loginTime = rs.getLong("login_time");
                if (loginTime <= 0) {
                    continue;
                }
                json.put(String.valueOf(serverId), loginTime);
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
