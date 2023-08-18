package game.entity;

import com.alibaba.fastjson.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
     * 查找所有游戏
     */
    public Set<String> queryNameByServerIds(List<Integer> serverIds) {
        Set<String> res = new HashSet<>();
        Connection conn = HikariDBPool.getDataConn();
        StringBuffer sb = new StringBuffer();
        int index = 0;
        for (Integer i : serverIds) {
            sb.append(i);
            if (index < serverIds.size() - 1) {
                sb.append(",");
            }
            index++;
        }
        String sql = "select * from users where server_id in (" + sb.toString() + ")";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                res.add(name);
            }
            return res;
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
    public int insertUserInfo(UserInfo userInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "replace into users(`name`,`combine_id`,`operator_id`,`server_id`,`level`,`player_name`,`login_time`) values(?,?,?,?,?,?,?)";
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
            String sql = "update users set player_name = ?,login_time=?,`level`=?  where combine_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, userInfo.getPlayerName());
            ps.setObject(2, userInfo.getLastLoginTime());
            ps.setObject(3, userInfo.getLevel());
            ps.setObject(4, userInfo.getCombineId());
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
        String sql = "select server_id,login_time,player_name,`level` from `users` where `name` ='" + accountName + "'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                int serverId = rs.getInt("server_id");
                long loginTime = rs.getLong("login_time");
                String playerName = rs.getString("player_name");
                int level = rs.getInt("level");
                if (loginTime <= 0) {
                    continue;
                }
                String sid = String.valueOf(serverId);

                JSONObject j = new JSONObject();
                j.put("login_time", loginTime);
                j.put("player_name", playerName);
                j.put("level", level);
                json.put(sid, j);
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
