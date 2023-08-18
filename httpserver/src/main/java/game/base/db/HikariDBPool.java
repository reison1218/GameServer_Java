/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.db;

import com.google.common.io.ByteStreams;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import game.base.config.Config;
import game.base.config.ConfigKey;
import game.utils.CommonUtils;
import game.utils.Log;
import game.utils.ScriptRunner;
import game.utils.StringEncoder;

import static com.google.common.base.Preconditions.checkArgument;

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
            //检查数据库
            check();
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

    public static void check() {
        try {
            // 检查db_version表是否存在
            if (isEmptyDataBase()) {
                Log.info("数据库里不存在表, 重新创建所有的表");

                createTables();

                if (isEmptyDataBase()) {
                    throw new RuntimeException("重新创建了表, 还是不存在任何表");
                }

            }
            //检查db_version表是否存在，不存在就创建一个
            checkDbVersionTable();
            int currentDBVersion = getDBVersion();
            Log.info("当前数据库版本: " + currentDBVersion);

            if (currentDBVersion != DBUpgrade.DB_VERSION) {
                if (currentDBVersion < DBUpgrade.DB_VERSION) {
                    try (Connection conn = getDataConn()) {
                        new DBUpgrade().upgradeDB(currentDBVersion, DBUpgrade.DB_VERSION, conn);
                    }
                } else {
                    throw new RuntimeException("当前数据库版本" + currentDBVersion + ", 程序使用的版本太旧: " + DBUpgrade.DB_VERSION);
                }
            }
        } catch (Exception e) {

        }
    }

    public static void checkDbVersionTable() throws SQLException {
        String sql = "SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'db_version'";

        String createSql = "CREATE TABLE `db_version` (`version` int NOT NULL DEFAULT 0,PRIMARY KEY (`version`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci";
        String insertSql = "insert into db_version(version) values(0)";

        try (Connection conn = getDataConn(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return;
            }

            Log.info("不存在db_version表，现在开始创建");
            stmt.execute(createSql);
            stmt.execute(insertSql);
        }
    }

    static int getDBVersion() throws SQLException {
        try (Connection conn = getDataConn(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from db_version");
            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    private static void createTables() throws IOException, SQLException {
        InputStream is = CommonUtils.getInputStreamFromClassPath("db_schema.sql");
        if (is == null) {
            Log.error("jar包中没有找到db_schema.sql");
            throw new RuntimeException();
        }
        try {
            byte[] data = ByteStreams.toByteArray(is);
            String sql = StringEncoder.encode(data);

            checkArgument(!sql.contains("/*"), "db_schema中不能存在/*的注释");

            checkArgument(!sql.contains("delimiter ;;"), "db_schema中不能有delimiter ;;");

            try (Connection conn = HikariDBPool.getDataConn()) {
                ScriptRunner runner = new ScriptRunner(conn, false, true);
                runner.runScript(new StringReader(sql));
            }
        } finally {
            is.close();
        }
    }

    private static boolean isEmptyDataBase() throws SQLException {
        try (Connection conn = HikariDBPool.getDataConn()) {
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet rs = metadata.getTables(conn.getCatalog(), null, null, new String[]{"TABLE"});
            rs.next();
            return !rs.next();
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
    }

}
