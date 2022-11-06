/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import game.base.config.Config;
import game.base.config.ConfigKey;
import game.utils.Log;

/**
 * <pre>
 * Mysql连接池
 * </pre>
 *
 * @author reison
 * @time 2019年7月27日
 */
public final class HikariDBPool {

    public final static String dbUrl = "jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&autoReconnect=true&useSSL=false";

    /**
     * 用户数据连接池
     */
    public static HikariDataSource dataPool = null;

    /**
     * 日志数据连接池
     */
    public static HikariDataSource logPool = null;

    /**
     * <pre>
     * 初始化
     * </pre>
     */
    public final static boolean init() {
        Connection connData = null;
        Connection connLog = null;
        try {
            initDataPool();
            //initLogPool();
            connData = dataPool.getConnection();
            if (connData != null) {
                Log.info("用户数据库连接池初始化成功~");
            }
            //			connLog = logPool.getConnection();
            //			if (connLog != null) {
            //				Log.info("日志数据库连接池初始化成功~");
            //			}
            return true;
        } catch (Exception e) {
            Log.error("DB连接池初始化失败", e);
            return false;
        } finally {
            if (connData != null) {
                try {
                    connData.close();
                } catch (SQLException e) {
                    Log.error("", e);
                }
            }
            if (connLog != null) {
                try {
                    connLog.close();
                } catch (SQLException e) {
                    Log.error("", e);
                }
            }
        }
    }

    /**
     * <pre>
     * 初始化用户数据连接池
     * </pre>
     */
    private final static void initDataPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                String.format(dbUrl, Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.HOST), Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.PORT),
                        Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.DB_DATA)));
        config.setUsername(Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.USER));
        config.setPassword(Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.PASS));
        // 开启预编译(相同sql不用每次都编译)
        config.addDataSourceProperty("cachePrepStmts", "true");
        // 预编译缓存数量
        config.addDataSourceProperty("prepStmtCacheSize", "256");
        // 预编译sql长度限制
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        // 获取连接超时ms
        config.setConnectionTimeout(5000);
        // 最小空闲连接数
        config.setMinimumIdle(8);
        // 最大连接数(与MinimumIdle表示固定连接数)
        config.setMaximumPoolSize(8);
        System.out.println("jdbcUrl:" + config.getJdbcUrl());
        System.out.println("user:" + config.getUsername());
        System.out.println("pass:" + config.getPassword());
        // 初始化用户数据连接池
        dataPool = new HikariDataSource(config);
    }

    /**
     * <pre>
     * 初始化日志数据连接池
     * </pre>
     */
    private final static void initLogPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                String.format(dbUrl, Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.HOST), Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.PORT),
                        Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.DB_LOG)));
        config.setUsername(Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.USER));
        config.setPassword(Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.PASS));
        // 获取连接超时ms
        config.setConnectionTimeout(5000);
        // 最小空闲连接数
        config.setMinimumIdle(8);
        // 最大连接数(与MinimumIdle表示固定连接数)
        config.setMaximumPoolSize(8);
        // 初始化用户数据连接池
        logPool = new HikariDataSource(config);
    }

    /**
     * <pre>
     * 获取用户数据库连接
     * </pre>
     */
    public final static Connection getDataConn() {
        try {
            return dataPool.getConnection();
        } catch (SQLException e) {
            Log.error("获取用户数据库连接错误", e);
        }
        return null;
    }

    /**
     * <pre>
     * 获取日志数据库连接
     * </pre>
     */
    public final static Connection getLogConn() {
        try {
            return logPool.getConnection();
        } catch (SQLException e) {
            Log.error("获取日志数据库连接错误", e);
        }
        return null;
    }

    /**
     * <pre>
     * 停止数据连接池
     * </pre>
     */
    public final static void stop() {
        if (dataPool != null) {
            dataPool.close();
        }
        if (logPool != null) {
            logPool.close();
        }
    }

    /**
     * <pre>
     * 检测连接池
     * </pre>
     */
    public final static boolean checkDB() {
        Connection connData = null;
        //Connection connLog = null;
        try {
            connData = dataPool.getConnection();
            if (connData == null || !connData.isValid(2)) {
                Log.fatal("Game库连接池异常。。。");
                return false;
            }
            //connLog = logPool.getConnection();
            //			if (connLog == null || !connLog.isValid(2)) {
            //				Log.fatal("Log库连接池异常。。。");
            //				return false;
            //			}
        } catch (Exception e) {
            Log.fatal("数据库连接池检测异常。。。message:" + e.getMessage());
            return false;
        } finally {
            if (connData != null) {
                try {
                    connData.close();
                } catch (SQLException e) {
                    Log.error("", e);
                }
            }
            //			if (connLog != null) {
            //				try {
            //					connLog.close();
            //				} catch (SQLException e) {
            //					Log.error("", e);
            //				}
            //			}
        }
        return true;
    }


    public static void query(String sql) {

    }


    public static boolean executeSql(String sql) {

        if (dataPool == null) {
            init();
        }

        boolean result = false;
        PreparedStatement psm = null;
        try {
            Connection conn = getDataConn();
            if (dataPool == null || conn == null || conn.isClosed()) {
                init();
            }

            psm = conn.prepareStatement(sql);
            result = psm.execute();
            psm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws SQLException {
        Log.init(HikariDBPool.class);
        Config.init();
        init();
        // System.out.println(dataPool.getJdbcUrl());
        // System.out.println(getLogConn());
    }

}
