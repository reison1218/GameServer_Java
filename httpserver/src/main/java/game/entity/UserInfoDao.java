package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;

import game.base.db.HikariDBPool;
import game.utils.Log;

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
            String sql = "replace into users(name,combine_id,operator_id,server_id,player_name) values(?,?,?,?,?)";
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
            String sql = "update t_users set player_name = ?  where combine_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, userInfo.getPlayerName());
            ps.setObject(2, userInfo.getCombineId());
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
}
