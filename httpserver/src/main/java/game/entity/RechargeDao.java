package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.base.db.HikariDBPool;

/**
 * @author tangjian
 * @date 2022-10-25 11:17
 * desc
 */
public class RechargeDao {



    private static final RechargeDao instance = new RechargeDao();

    public static final RechargeDao getInstance() {
        return instance;
    }

    /**
     * 查找所有游戏
     * @return
     */
    public Map<String, RechargeInfo> findRechargeInfos() {

        Map<String, RechargeInfo> map = new ConcurrentHashMap<>();
        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * from recharge";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            List<RechargeInfo> list = null;
            while (rs.next()) {
                String orderId = rs.getString("order_id");
                String operatorOrderId = rs.getString("operator_order_id");
                long combineId = rs.getLong("combine_id");
                int operatorId = rs.getInt("operator_id");
                int serverId = rs.getInt("server_id");
                int userId = rs.getInt("user_id");
                String itemId = rs.getString("item_id");
                int rmb = rs.getInt("rmb");
                int gold = rs.getInt("gold");
                int time = rs.getInt("time");
                int processTime = rs.getInt("process_time");
                byte[] misc1 = rs.getBytes("misc1");
                byte[] misc2 = rs.getBytes("misc2");
                RechargeInfo gi = new RechargeInfo();
                gi.setOrderId(orderId);
                gi.setOperatorOrderId(operatorOrderId);
                gi.setCombineId(combineId);
                gi.setOperatorId(operatorId);
                gi.setServerId(serverId);
                gi.setUserId(userId);
                gi.setItemId(itemId);
                gi.setRmb(rmb);
                gi.setGold(gold);
                gi.setTime(time);
                gi.setProcessTime(processTime);
                gi.setMisc1(misc1);
                gi.setMisc2(misc2);
                map.put(gi.getOrderId(),gi);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(ps!=null){
                    ps.close();
                }
                if(rs!=null){
                    rs.close();
                }
                if(conn!=null){
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
