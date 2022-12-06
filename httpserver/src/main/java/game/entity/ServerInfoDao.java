package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.base.db.HikariDBPool;

public class ServerInfoDao {


    private static final ServerInfoDao instance = new ServerInfoDao();

    public static final ServerInfoDao getInstance() {
        return instance;
    }

    /**
     * 查找所有游戏
     */
    public Map<Integer, ServerInfo> findServerInfos() {

        Map<Integer, ServerInfo> map = new ConcurrentHashMap<>();
        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * from server_list";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                int serverId = rs.getInt("server_id");
                String name = rs.getString("name");
                String ws = rs.getString("ws");
                String openTime = rs.getString("open_time");
                int registerState = rs.getInt("register_state");
                int state = rs.getInt("state");
                int letter = rs.getInt("letter");
                int targetServerId = rs.getInt("target_server_id");
                int mergeTimes = rs.getInt("merge_times");
                String type = rs.getString("type");
                String rechargeHttpUrl = rs.getString("recharge_http_url");
                ServerInfo gi = new ServerInfo();
                gi.setServerId(serverId);
                gi.setName(name);
                gi.setWs(ws);
                gi.setOpenTime(openTime);
                gi.setRegisterState(registerState);
                gi.setState(state);
                gi.setLetter(letter);
                gi.setTargetServerId(targetServerId);
                gi.setMergeTimes(mergeTimes);
                gi.setType(type);
                gi.setRechargeHttpUrl(rechargeHttpUrl);
                map.put(gi.getServerId(), gi);
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

}
