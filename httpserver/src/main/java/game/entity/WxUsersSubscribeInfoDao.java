package game.entity;

import com.alibaba.fastjson.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.base.db.HikariDBPool;

/**
 * @author tangjian
 * @date 2023-07-26 15:49
 * desc
 */
public class WxUsersSubscribeInfoDao {
    private static final WxUsersSubscribeInfoDao instance = new WxUsersSubscribeInfoDao();

    public static WxUsersSubscribeInfoDao getInstance() {
        return instance;
    }


    public WxUsersSubscribeInfo findWxUsersSubscribeInfo(String accountName) {

        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * where `name` ='" + accountName + "'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                String openId = rs.getString("open_id");
                String templIds = rs.getString("templ_ids");

                WxUsersSubscribeInfo gi = new WxUsersSubscribeInfo();
                gi.setName(name);
                gi.setOpenId(openId);
                gi.setTemplIdMap(templIds);
                return gi;
            }
            return null;
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
     * 查找所有游戏
     */
    public Map<String, WxUsersSubscribeInfo> queryWxUsersSubscribeInfo() {

        Map<String, WxUsersSubscribeInfo> map = new ConcurrentHashMap<>();
        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * from wx_users_subscribe";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                String openId = rs.getString("open_id");
                String templIds = rs.getString("templ_ids");

                WxUsersSubscribeInfo gi = new WxUsersSubscribeInfo();
                gi.setName(name);
                gi.setOpenId(openId);
                gi.setTemplIdMap(templIds);
                map.put(gi.getName(), gi);
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
    public int insertUserInfo(WxUsersSubscribeInfo wxUsersSubscribeInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "replace into wx_users_subscribe(name,open_id,templ_ids) values(?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setObject(1, wxUsersSubscribeInfo.getName());
            ps.setObject(2, wxUsersSubscribeInfo.getOpenId());
            ps.setObject(3, wxUsersSubscribeInfo.getTemplIdMapStr());
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
