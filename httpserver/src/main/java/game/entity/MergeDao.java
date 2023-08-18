package game.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import game.base.db.HikariDBPool;

/**
 * @author tangjian
 * @date 2023-05-06 17:49
 * desc
 */
public class MergeDao {

    private static final MergeDao instance = new MergeDao();

    public static final MergeDao getInstance() {
        return instance;
    }

    /**
     * 查找所有游戏
     */
    public boolean queryReload() {

        Connection conn = HikariDBPool.getDataConn();
        String sql = "select * from merge_change";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int reload = 0;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);

            while (rs.next()) {
                reload = rs.getInt("reload");
            }
            return reload == 1;
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
        return reload >= 1;
    }

    /**
     * 查找所有游戏
     */
    public void clearReload() {

        Connection conn = HikariDBPool.getDataConn();
        String sql = "delete from  merge_change";
        PreparedStatement ps = null;
        int reload = 0;
        try {
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
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
    }
}
