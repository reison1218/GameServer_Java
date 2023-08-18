package game.base.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import game.utils.Log;

/**
 * @author tangjian
 * @date 2023-03-08 18:02
 * desc
 */
public class DBUpgrade {

    /**
     * 当前数据库表结构的版本号
     */
    public static final int DB_VERSION = 7;

    public DBUpgrade() {
    }

    public void upgradeDB(int fromVersion, int toVersion, Connection conn) throws SQLException {
        for (int version = fromVersion; version < toVersion; version++) {
            upgrade(version, conn);
        }
    }

    private void upgrade(int fromVersion, Connection conn) throws SQLException {

        checkDBVersion(conn);

        switch (fromVersion) {
            case 0:
                upgrade0to1(conn);
                return;
            case 1:
                upgrade1to2(conn);
                return;
            case 2:
                upgrade2to3(conn);
                return;
            case 3:
                upgrade3to4(conn);
                return;
            case 4:
                upgrade4to5(conn);
                return;
            case 5:
                upgrade5to6(conn);
                return;
            case 6:
                upgrade6to7(conn);
                return;
            default: {
                throw new IllegalArgumentException("要升级数据库, 但是不知道怎么升: fromVersion " + fromVersion);
            }
        }
    }

    public void checkDBVersion(Connection conn) {
        String sql0 =
                "CREATE TABLE IF NOT EXISTS  `db_version` (`version` int NOT NULL, PRIMARY KEY (`version`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
        String sql1 = "select version from  `db_version`";
        String sql2 = "insert into `db_version` values(0)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();
            if (!rs.next()) {
                ps.executeUpdate(sql2);
            }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean checkField(String tableName, String column, Connection conn) {
        String checkSql = "SHOW COLUMNS FROM " + tableName + " LIKE '" + column + "'";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(checkSql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (Exception e) {

        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void upgrade0to1(Connection conn) throws SQLException {
        Log.info("升级数据库, 从0号升级到1号");

        String sql0 =
                "ALTER TABLE `server_list` ADD COLUMN `server_type` INT DEFAULT 0 NULL COMMENT '是否版署服（0：不是 1是）' AFTER `merge_times`,CHARSET=utf8mb3;";

        String setVersion = "update db_version set version=1 where version=0";

        conn.setAutoCommit(false);
        if (!checkField("server_list", "server_type", conn)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }

    private void upgrade1to2(Connection conn) throws SQLException {
        Log.info("升级数据库, 从1号升级到2号");

        String sql0 = "ALTER TABLE `server_list` ADD COLUMN `manager` VARCHAR(128) NULL COMMENT 'http' AFTER `merge_times`, CHARSET=utf8mb3;";

        String setVersion = "update db_version set version=2 where version=1";

        conn.setAutoCommit(false);

        if (!checkField("server_list", "manager", conn)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }

    private void upgrade2to3(Connection conn) throws SQLException {
        Log.info("升级数据库, 从2号升级到3号");
        String sql0 = "ALTER TABLE `server_list` ADD COLUMN `inner_manager` VARCHAR(128) NULL COMMENT '内网http' AFTER `manager`,CHARSET=utf8mb3;";

        String setVersion = "update db_version set version=3 where version=2";

        conn.setAutoCommit(false);

        if (!checkField("server_list", "inner_manager", conn)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }


    private void upgrade3to4(Connection conn) throws SQLException {
        Log.info("升级数据库, 从3号升级到4号");
        String sql0 = "CREATE TABLE IF NOT EXISTS  `merge_change` ( `reload` INT NOT NULL DEFAULT 0 ) ENGINE=INNODB CHARSET=utf8mb3;";

        String setVersion = "update db_version set version=4 where version=3";

        conn.setAutoCommit(false);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }


    private void upgrade4to5(Connection conn) throws SQLException {
        Log.info("升级数据库，从4号升级到5号");
        String sql0 =
                "ALTER TABLE `server_list` ADD COLUMN `update_merge_times_time` DATETIME DEFAULT NULL COMMENT '修改merge_times的时间' AFTER `merge_times`,CHARSET=utf8mb3;";
        String setVersion = "update db_version set version=5 where version=4";
        conn.setAutoCommit(false);

        if (!checkField("server_list", "update_merge_times_time", conn)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }


    private void upgrade5to6(Connection conn) throws SQLException {
        Log.info("升级数据库，从5号升级到6号");
        String sql0 = "ALTER TABLE `users` ADD COLUMN `level` INT DEFAULT 0 NULL COMMENT '基地等级' AFTER `server_id`, CHARSET=utf8mb3";
        String setVersion = "update db_version set version=6 where version=5";
        conn.setAutoCommit(false);

        if (!checkField("`users`", "level", conn)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }

    private void upgrade6to7(Connection conn) throws SQLException {
        Log.info("升级数据库，从6号升级到7号");
        String sql0 = "CREATE TABLE `wx_users_subscribe` (`name` VARCHAR(128) NOT NULL COMMENT '玩家账号',\n" +
                "  `open_id` VARCHAR(128) COMMENT '玩家微信open_id',\n" +
                "  `templ_ids` VARCHAR(1024) COMMENT '玩家订阅的消息膜拜id',\n" +
                "  PRIMARY KEY (`name`) \n" + ") ENGINE=INNODB CHARSET=utf8mb4;";
        String setVersion = "update db_version set version=7 where version=6";
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(setVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn.commit();
        conn.setAutoCommit(true);
    }
}


