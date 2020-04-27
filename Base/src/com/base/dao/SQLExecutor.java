/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.dbpool.HikariDBPool;
import com.base.module.JsonDataModule;
import com.base.module.TableType;
import com.utils.JsonUtil;
import com.utils.Log;

/**
 * <pre>
 * SQL执行器
 * JSON
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class SQLExecutor {

	/** 模块用户表创建语句(单玩家多条数据) */
	public final static String TABLE_CREATE_SQL = "CREATE TABLE `t_u_%s` ("
			+ "`Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',"
			+ "`UserId` int(11) NOT NULL DEFAULT '0' COMMENT '用户Id',"
			+ "`Other` varchar(50) NOT NULL DEFAULT '' COMMENT '扩展参数',"
			+ "`Content` json DEFAULT NULL COMMENT 'json数据'," + "PRIMARY KEY (`Id`),"
			+ "UNIQUE KEY `idx_userid_other` (`UserId`,`Other`) USING BTREE"
			+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='%s数据表';";

	/** 简单模块用户表创建语句 (单玩家单条数据) */
	public final static String TABLE_CREATE_SQL_SIMPLE = "CREATE TABLE `t_u_%s` (`UserId` int(11) NOT NULL DEFAULT '0' COMMENT '用户Id',`Content` json DEFAULT NULL COMMENT 'json数据',PRIMARY KEY (`UserId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='%s数据表';";

	/** 简单模块用户表创建语句 (单玩家多条数据，UserId+Tid两个主键) */
	public final static String TABLE_CREATE_SQL_TEMP = "CREATE TABLE `t_u_%s` (`UserId` int(11) NOT NULL DEFAULT '0' COMMENT '用户Id',`Tid` int(11) NOT NULL DEFAULT '0' COMMENT '模板Id',`Content` json DEFAULT NULL COMMENT 'json数据',PRIMARY KEY (`UserId`,`Tid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='%s数据表';";

	/** 用户日志表创建语句(单玩家多条数据) */
	public final static String LOG_TABLE_CREATE = "CREATE TABLE `t_log_%s` ("
			+ "`Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增Id',"
			+ "`UserId` int(11) NOT NULL DEFAULT '0' COMMENT '用户Id',"
			+ "`CreateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',"
			+ "`Content` json NOT NULL COMMENT '操作内容'," + "PRIMARY KEY (`Id`)"
			+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='%s';";

	/**
	 * <pre>
	 * 执行select语句
	 * 返回[{<字段名1，字段值1>,<字段名2，字段值2>,<字段名3，字段值3>},{<字段名1，字段值1>,<字段名2，字段值2>,<字段名3，字段值3>}...]
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public final static List<Map> execSelect(String sql, Object... params) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		List<Map> list = null;
		ResultSet rs = null;
		try {
			conn = HikariDBPool.getDataConn();
			pstmt = conn.prepareStatement(sql);
			prepareCommand(pstmt, params);
			rs = pstmt.executeQuery();
			ResultSetMetaData metaData = pstmt.getMetaData();
			int columnCount = 0;
			if (metaData != null) {
				columnCount = metaData.getColumnCount();
			}

			while (columnCount > 0 && rs.next()) {
				if (list == null) {
					list = new ArrayList<>();
				}
				Map<String, Object> temp = new HashMap<>();
				list.add(temp);
				for (int i = 1; i <= columnCount; i++) {
					temp.put(metaData.getColumnLabel(i), rs.getObject(i));
				}
			}
		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return list;
	}

	/**
	 * <pre>
	 * 执行Json简单查询
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static Map<String, Object> execSelectJSON(String sql, Object... params) {
		Map<String, Object> jsonMap = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			// 创建一个封装json的Map
			conn = HikariDBPool.getDataConn();
			// 接收sql语句
			pstmt = conn.prepareStatement(sql);
			// 给给定的参数赋值
			prepareCommand(pstmt, params);
			// 执行查询语句返回结果集
			rs = pstmt.executeQuery();
			// 遍历结果集
			if (!rs.next()) {
				return null;
			}
			// 把结果内容解析成一个map{key:value...}返回
			jsonMap = (Map<String, Object>) JsonUtil.parse(rs.getString("Content"));
		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return jsonMap;
	}

	/**
	 * <pre>
	 * 执行SQL语句，查询templateId，Content简单查询
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 * @return Map<Integer , JSONObject>
	 */
	public final static Map<Integer, JSONObject> execSelectTemJSON(String sql, Object... params) {
		Map<Integer, JSONObject> temJsonMap = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			// 创建一个封装json的Map
			conn = HikariDBPool.getDataConn();
			// 接收sql语句
			pstmt = conn.prepareStatement(sql);
			// 给给定的参数赋值
			prepareCommand(pstmt, params);
			// 执行查询语句返回结果集
			rs = pstmt.executeQuery();
			// 遍历结果集
			while (rs.next()) {
				JSONObject jsonMap = new JSONObject();
				int templateId = rs.getInt("Tid");
				// 把结果内容解析成一个map{key:value...}
				jsonMap = (JSONObject) JsonUtil.parse(rs.getString("Content"));
				if (temJsonMap == null) {
					temJsonMap = new ConcurrentHashMap<Integer, JSONObject>();
				}
				temJsonMap.put(templateId, jsonMap);
			}
			// 把结果内容解析成一个map{key:value...}返回
			// jsonMap = (Map<String, Object>) JsonUtil.parse(rs.getString("Content"));
		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return temJsonMap;
	}

	/**
	 * <pre>
	 * 执行Data库update,delete,create table等没有结果集返回的语句
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 */
	public final static boolean execUpdate(String sql, Object... params) {
		return execUpdate(sql, false, params);
	}

	/**
	 * <pre>
	 * 执行Log库update,delete,create table等没有结果集返回的语句
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 */
	public final static boolean execLogUpdate(String sql, Object... params) {
		return execUpdate(sql, true, params);
	}

	/**
	 * <pre>
	 * 执行update,delete,create table等没有结果集返回的语句
	 * </pre>
	 *
	 * @param sql
	 * @param isLog  是否为Log库SQL
	 * @param params
	 * @return
	 */
	private final static boolean execUpdate(String sql, boolean isLog, Object... params) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		int effectCount = 0;
		try {
			if (isLog) {
				conn = HikariDBPool.getLogConn();
			} else {
				conn = HikariDBPool.getDataConn();
			}
			pstmt = conn.prepareStatement(sql);
			prepareCommand(pstmt, params);
			effectCount = pstmt.executeUpdate();
			if (effectCount == 0) {
				// Log.error("执行sql没有影响到行：\nsql:" + sql + "\nparams:" +
				// Arrays.toString(params));
				return false;
			}
		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
			return false;
		} finally {
			close(conn, pstmt, null);
		}
		// Log.info("sql语句执行成功:\n" + sql + "\nparams:" + Arrays.toString(params));
		return true;
	}

	/**
	 * <pre>
	 * 执行Data库insert
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 */
	public final static int execInsert(String sql, Object... params) {
		return execInsert(sql, false, params);
	}

	/**
	 * <pre>
	 * 执行Log库insert
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 */
	public final static int execLogInsert(String sql, Object... params) {
		return execInsert(sql, true, params);
	}

	/**
	 * <pre>
	 * 执行insert
	 * </pre>
	 *
	 * @param sql
	 * @param isLog  是否为Log库SQL
	 * @param params
	 * @return
	 */
	private final static int execInsert(String sql, boolean isLog, Object... params) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		int effectCount = 0;
		try {
			if (isLog) {
				conn = HikariDBPool.getLogConn();
			} else {
				conn = HikariDBPool.getDataConn();
			}
			pstmt = conn.prepareStatement(sql);
			prepareCommand(pstmt, params);
			effectCount = pstmt.executeUpdate();
			if (effectCount == 0) {
				// Log.error("执行sql没有影响到行：\nsql:" + sql + "\nparams:" +
				// Arrays.toString(params));
			}
			// Log.info("sql语句执行成功:\n" + sql + "\nparams:" + Arrays.toString(params));
		} catch (Exception e) {
			// Log.error("sql语句执行错误\nsql:" + sql + "\nparams:" + Arrays.toString(params),
			// e);
			e.printStackTrace();
		} finally {
			close(conn, pstmt, null);
		}
		return effectCount;
	}

	/**
	 * <pre>
	 * 执行insert并返回自增主键
	 * </pre>
	 *
	 * @param sql
	 * @param params
	 */
	public final static int execInsertReturnId(String sql, Object... params) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		int effectCount = 0;
		try {
			conn = HikariDBPool.getDataConn();
			pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			prepareCommand(pstmt, params);
			effectCount = pstmt.executeUpdate();
			if (effectCount == 0) {
				Log.error("执行sql没有影响到行：\nsql:" + sql + "\nparams:" + Arrays.toString(params));
			}
			// Log.info("sql语句执行成功:\n" + sql + "\nparams:" + Arrays.toString(params));
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			Log.error("sql语句执行错误\nsql:" + sql + "\nparams:" + Arrays.toString(params), e);
			return 0;
		} finally {
			close(conn, pstmt, null);
		}
		return 0;
	}

	/**
	 * <pre>
	 * 赋值参数
	 * </pre>
	 *
	 * @param pstmt
	 * @param params
	 * @throws Exception
	 */
	private final static void prepareCommand(PreparedStatement pstmt, Object... params) throws Exception {
		if (pstmt == null || params == null)
			return;
		for (int i = 0, len = params.length; i < len; i++) {
			pstmt.setObject(i + 1, params[i]);
		}
	}

	/**
	 * <pre>
	 * 关闭连接
	 * </pre>
	 *
	 * @param conn
	 * @param pstmt
	 * @param rs
	 */
	private final static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			Log.error("", e);
		}
		try {
			if (pstmt != null) {
				// BenchMarkMgr.addSQL(pstmt.toString());
				pstmt.clearParameters();
				pstmt.close();
				pstmt = null;
			}
		} catch (Exception e) {
			Log.error("", e);
		}
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			Log.error("", e);
		}
	}

	/**
	 * <pre>
	 * 某表是否已创建
	 * </pre>
	 *
	 * @param module
	 * @return
	 */
	public final static boolean isTableExist(String module) {
		String sql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` =?;";
		@SuppressWarnings("rawtypes")
		List<Map> temp = execSelect(sql, Config.getConfig(ConfigKey.MYSQL_CONF, ConfigKey.DB_DATA), "t_u_" + module);
		if (temp != null && !temp.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 创建表
	 * </pre>
	 *
	 * @param module
	 * @param comment
	 * @return
	 */
	public final static void createTable(String module, String comment, TableType tableType) {
		// String tableSql = TABLE_CREATE_SQL_SIMPLE;
		// if (isMulti) {
		// tableSql = TABLE_CREATE_SQL;
		// }
		String tableSql = tableType.getSql();
		tableSql = String.format(tableSql, module, comment);
		SQLExecutor.execUpdate(tableSql);
	}

	/**
	 * <pre>
	 * 检测表是否存在，不存在则创建表
	 * </pre>
	 *
	 * @param module
	 * @param comment
	 * @return
	 */
	public final static boolean checkCreateTable(String module, String comment, TableType tableType) {
		if (module == null || module.equals("") || tableType == null) {
			return true;
		}
		module = module.toLowerCase();
		if (!isTableExist(module)) {
			createTable(module, comment, tableType);
			return true;
		}
		return true;
	}

	public static void main(String[] args) {
		Log.init(SQLExecutor.class);
		// 地壳初始化
		if (!Config.init()) {
			return;
		}

		HikariDBPool.init();
		for (int i = 0; i < 1000000000; i++)
			;
		/**
		 * <pre>
		 * 大表插入，字节数：47829,执行5000次耗时：73363ms,平均耗时:14ms
		 * 大表整json更新，字节数：47807,执行5000次耗时：108708ms,平均耗时:21ms
		 * 大表单Key Update,执行5000次耗时：52426ms,平均耗时:10ms
		 * </pre>
		 */
		testBigInsert();
		testColumnBigUpdate();
		testKeyBigUpdate();
		/**
		 * <pre>
		 * 小表插入， 字节数：181,执行5000次耗时：6202ms,平均耗时:1ms
		 * 小表整json更新，字节数：181,执行5000次耗时：3193ms,平均耗时:0ms
		 * 小表单Key-Update,执行5000次耗时：3758ms,平均耗时:0ms
		 * </pre>
		 */
		testSmallInsert();
		testColumnSmallUpdate();
		testKeySmallUpdate();

		testConditionDataInsert();
		testKeyConditionUpdate();
		testColumnConditionUpdate();
	}

	public final static void testKeyBig2Update() {
		String sql = "UPDATE `t_u_%s` SET `Content` = JSON_SET(`Content`,'$.key0','newvalue01') WHERE `UserId` = ?;";
		String tempSql = String.format(sql, "large");
		long st = System.currentTimeMillis();
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("大表单Key Update,执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 字节数：47807,执行5000次耗时：247753ms,平均耗时:49ms
	 * </pre>
	 */
	public final static void testColumnBig2Update() {
		String sql = JsonDataModule.UPDATE_SQL;
		String tempSql = String.format(sql, "large");
		final int dataCount = 1999;
		List<Integer> list0 = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list0.add(i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, "value" + i);
			if (i % 100 == 0) {
				dataMap.put("list" + i, list0);
			}
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, json, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("大表整json更新，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:"
				+ (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * </pre>
	 */
	public final static void testBig2Insert() {
		String sql = JsonDataModule.INSERT_SQL;
		String tempSql = String.format(sql, "large");
		final int dataCount = 2000;
		Map<String, Object> keyMap = new HashMap<>();
		for (int i = 0; i < 50; i++) {
			keyMap.put("subKey" + i, "subValue" + i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, keyMap);
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 1;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execInsert(tempSql, i, json);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println(
				"大表插入，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	public final static void testKeyBigUpdate() {
		String sql = "UPDATE `t_u_%s` SET `Content` = JSON_SET(`Content`,'$.key0','newvalue01') WHERE `UserId` = ?;";
		String tempSql = String.format(sql, "large");
		long st = System.currentTimeMillis();
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("大表单Key Update,执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 字节数：47807,执行5000次耗时：247753ms,平均耗时:49ms
	 * </pre>
	 */
	public final static void testColumnBigUpdate() {
		String sql = JsonDataModule.UPDATE_SQL;
		String tempSql = String.format(sql, "large");
		final int dataCount = 1999;
		List<Integer> list0 = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list0.add(i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, "value" + i);
			if (i % 100 == 0) {
				dataMap.put("list" + i, list0);
			}
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, json, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("大表整json更新，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:"
				+ (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 数据量：4000,执行5000次耗时：227737ms,平均耗时:45ms
	 * </pre>
	 */
	public final static void testBigInsert() {
		String sql = JsonDataModule.INSERT_SQL;
		String tempSql = String.format(sql, "large");
		final int dataCount = 2000;
		List<Integer> list0 = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list0.add(i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, "value" + i);
			if (i % 100 == 0) {
				dataMap.put("list" + i, list0);
			}
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execInsert(tempSql, i, json);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println(
				"大表插入，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	public final static void testKeySmallUpdate() {
		String sql = "UPDATE `t_u_%s` SET `Content` = JSON_SET(`Content`,'$.key0','newvalue01') WHERE `UserId` = ?;";
		String tempSql = String.format(sql, "small");
		long st = System.currentTimeMillis();
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("小表单Key-Update,执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 字节数：47807,执行5000次耗时：247753ms,平均耗时:49ms
	 * </pre>
	 */
	public final static void testColumnSmallUpdate() {
		String sql = JsonDataModule.UPDATE_SQL;
		String tempSql = String.format(sql, "small");
		final int dataCount = 10;
		List<Integer> list0 = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			list0.add(i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, "value" + i);
			if (i % 100 == 0) {
				dataMap.put("list" + i, list0);
			}
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, json, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("小表整json更新，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:"
				+ (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 数据量：4000,执行5000次耗时：227737ms,平均耗时:45ms
	 * </pre>
	 */
	public final static void testSmallInsert() {
		String sql = JsonDataModule.INSERT_SQL;
		String tempSql = String.format(sql, "small");
		final int dataCount = 10;
		List<Integer> list0 = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			list0.add(i);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < dataCount; i++) {
			dataMap.put("key" + i, "value" + i);
			if (i % 100 == 0) {
				dataMap.put("list" + i, list0);
			}
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 5000;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execInsert(tempSql, i, json);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println(
				"小表插入， 字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 小表插入， 字节数：85291,执行100次耗时：4089ms,平均耗时:40ms
	 * </pre>
	 */
	public final static void testConditionDataInsert() {
		String truncate = "TRUNCATE TABLE `t_u_small`;";
		SQLExecutor.execUpdate(truncate);
		for (int i = 0; i < Integer.MAX_VALUE; i++)
			;
		String sql = JsonDataModule.INSERT_SQL;
		String tempSql = String.format(sql, "small");
		Map<String, Object> oneCondition = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			oneCondition.put("conKey_" + i, "conValue_" + i);
		}
		List<Object> list0 = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			list0.add(oneCondition);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < 2; i++) {
			dataMap.put("type_" + i, list0);
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 100;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execInsert(tempSql, i, json);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println(
				"小表插入， 字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 小表单Key-Update,执行100次耗时：2456ms,平均耗时:24ms
	 * </pre>
	 */
	public final static void testKeyConditionUpdate() {
		Map<String, Object> oneCondition = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			oneCondition.put("conKey1_" + i, "conValue1_" + i);
		}
		List<Object> list0 = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			list0.add(oneCondition);
		}
		String sql = "UPDATE `t_u_%s` SET `Content` = JSON_SET(`Content`,'$.type_25','" + JsonUtil.stringify(list0)
				+ "') WHERE `UserId` = ?;";
		String tempSql = String.format(sql, "small");
		long st = System.currentTimeMillis();
		final int count = 100;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("小表单Key-Update,执行" + count + "次耗时：" + ct + "ms,平均耗时:" + (ct / count) + "ms");
	}

	/**
	 * <pre>
	 * 小表整json更新，字节数：85341,执行100次耗时：2890ms,平均耗时:28ms
	 * </pre>
	 */
	public final static void testColumnConditionUpdate() {
		String sql = JsonDataModule.UPDATE_SQL;
		String tempSql = String.format(sql, "small");
		Map<String, Object> oneCondition = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			oneCondition.put("conKey_" + i, "conValue_" + i);
		}
		List<Object> list0 = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			list0.add(oneCondition);
		}
		Map<String, Object> dataMap = new HashMap<>();
		for (int i = 0; i < 50; i++) {
			dataMap.put("type1_" + i, list0);
		}
		long st = System.currentTimeMillis();
		final String json = JsonUtil.stringify(dataMap);
		final int count = 100;
		for (int i = 0; i < count; i++) {
			SQLExecutor.execUpdate(tempSql, json, i);
		}
		long ct = System.currentTimeMillis() - st;
		System.out.println("小表整json更新，字节数：" + json.getBytes().length + ",执行" + count + "次耗时：" + ct + "ms,平均耗时:"
				+ (ct / count) + "ms");
	}

	@SuppressWarnings("unchecked")
	public final static Map<String, Map<String, Object>> execSelectJSONMap(String sql, Object... params) {
		Map<String, Map<String, Object>> jsonMap = null;
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			// 创建一个封装json的Map
			conn = HikariDBPool.getDataConn();
			// 接收sql语句
			pstmt = conn.prepareStatement(sql);
			// 给给定的参数赋值
			prepareCommand(pstmt, params);
			// 执行查询语句返回结果集
			rs = pstmt.executeQuery();
			// 遍历结果集
			if (!rs.next()) {
				return null;
			}
			// 把结果内容解析成一个map{key:value...}返回
			Map<String, Object> moduleMap = null;
			moduleMap = (Map<String, Object>) JsonUtil.parse(rs.getString("Content"));

			jsonMap = new HashMap<>();
			jsonMap.put(rs.getString("ModuleId"), moduleMap);
		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return jsonMap;
	}

	@SuppressWarnings("unchecked")
	public final static Map<String, Map<String, Object>> execSelectPlayerJSONMap(String sql, Object... params) {
		Map<String, Map<String, Object>> jsonMap = new HashMap<>();
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			// 创建一个封装json的Map
			conn = HikariDBPool.getDataConn();
			// 接收sql语句
			pstmt = conn.prepareStatement(sql);
			// 给给定的参数赋值
			prepareCommand(pstmt, params);
			// 执行查询语句返回结果集
			rs = pstmt.executeQuery();

			Map<String, Object> moduleMap = null;
			// 遍历结果集
			while (rs.next()) {
				// 把结果内容解析成一个map{key:value...}返回
				moduleMap = (Map<String, Object>) JsonUtil.parse(rs.getString("Content"));

				jsonMap.put(rs.getString("UserId"), moduleMap);
			}

		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return jsonMap;
	}
	
	
	@SuppressWarnings("unchecked")
	public final static List<Map<String, Object>> execSelectPlayerJSONList(String sql, Object... params) {
		List<Map<String, Object>> jsonList = new ArrayList<>();
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			// 创建一个封装json的Map
			conn = HikariDBPool.getDataConn();
			// 接收sql语句
			pstmt = conn.prepareStatement(sql);
			// 给给定的参数赋值
			prepareCommand(pstmt, params);
			// 执行查询语句返回结果集
			rs = pstmt.executeQuery();

			Map<String, Object> moduleMap = null;
			// 遍历结果集
			while (rs.next()) {
				// 把结果内容解析成一个map{key:value...}返回
				moduleMap = (Map<String, Object>) JsonUtil.parse(rs.getString("Content"));
				jsonList.add(moduleMap);
			}

		} catch (Exception e) {
			Log.error("sql语句执行错误,sql:\n" + sql + "\nparams:" + Arrays.toString(params), e);
		} finally {
			close(conn, pstmt, rs);
		}
		return jsonList;
	}

}
