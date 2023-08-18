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
                String manager = rs.getString("manager");
                String inner_manager = rs.getString("inner_manager");
                int serverType = rs.getInt("server_type");
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
                gi.setManager(manager);
                gi.setInnerManager(inner_manager);
                gi.setServerType(serverType);
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

    /**
     * 新增用户
     */
    public int update(ServerInfo serverInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result;
        try {
            String sql = "update server_list set name=?,ws=?,open_time=?,register_state=?,state=?,letter=?,target_server_id=?,merge_times=?,type=?,manager=?,inner_manager=?,server_type=? where server_id=?";

            ps = conn.prepareStatement(sql);
            ps.setString(1,serverInfo.getName());
            ps.setString(2,serverInfo.getWs());
            ps.setString(3,serverInfo.getOpenTime());
            ps.setInt(4,serverInfo.getRegisterState());
            ps.setInt(5,serverInfo.getState());
            ps.setInt(6,serverInfo.getLetter());
            ps.setInt(7,serverInfo.getTargetServerId());
            ps.setInt(8,serverInfo.getMergeTimes());
            ps.setString(9,serverInfo.getTypeStr());
            ps.setString(10,serverInfo.getManager());
            ps.setString(11,serverInfo.getInnerManager());
            ps.setInt(12,serverInfo.getServerType());
            ps.setInt(13,serverInfo.getServerId());
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
     * 新增用户
     */
    public int insert(ServerInfo serverInfo) {
        Connection conn = HikariDBPool.getDataConn();
        PreparedStatement ps = null;
        int result = 0;
        try {
            String sql =
                    "INSERT INTO server_list (server_id, name, ws, open_time, register_state, state, letter, target_server_id, merge_times, `type`, manager,inner_manager,server_type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, serverInfo.getServerId());
            ps.setString(2, serverInfo.getName());
            ps.setString(3, serverInfo.getWs());
            ps.setString(4, serverInfo.getOpenTime());
            ps.setInt(5, serverInfo.getRegisterState());
            ps.setInt(6, serverInfo.getState());
            ps.setInt(7, serverInfo.getLetter());
            ps.setInt(8, serverInfo.getTargetServerId());
            ps.setInt(9, serverInfo.getMergeTimes());
            ps.setString(10, serverInfo.getTypeStr());
            ps.setString(11, serverInfo.getManager());
            ps.setString(12, serverInfo.getInnerManager());
            ps.setInt(13, serverInfo.getServerType());
            result = ps.executeUpdate();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
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
    }
}

