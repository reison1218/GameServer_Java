package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.base.db.HikariDBPool;

/**
 * @author tangjian
 * @date 2023-01-31 12:04
 * desc
 */
public class WhiteUserInfoDao {
    private static final WhiteUserInfoDao instance = new WhiteUserInfoDao();

    public static WhiteUserInfoDao getInstance() {
        return instance;
    }


    /**
     * 查找所有游戏
     */
    public Map<String, WhiteUserInfo> findWhiteUser() {

        Map<String, WhiteUserInfo> map = new ConcurrentHashMap<>();
        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * from white_users";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");

                WhiteUserInfo gi = new WhiteUserInfo();
                gi.setName(name);
                map.put(gi.name, gi);
            }
            return map;
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

    /**
     * 新增用户
     */
    public int insertUserInfo(WhiteUserInfo whiteUserInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "replace into white_users(name) values(?)";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, whiteUserInfo.name);
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

    public int deleteUserInfo(String name) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "delete from white_users where name=?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, name);
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
