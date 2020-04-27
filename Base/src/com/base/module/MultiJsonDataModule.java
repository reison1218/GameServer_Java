/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.base.dao.SQLExecutor;
import com.base.entity.JsonDataOption;
import com.base.module.able.MultiJsonable;
import com.game.player.GamePlayer;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.MathUtil;


/**
 * <pre>
 * 采用JSON格式存储数据的模块基类
 * ！一个玩家存在多条数据
 * </pre>
 * 
 * @author reison
 */
public abstract class MultiJsonDataModule extends BaseModule implements MultiJsonable {

	public MultiJsonDataModule(GamePlayer player) {
		super(player);
	}
	
	public MultiJsonDataModule(int userId) {
		super(userId);
	}

	/** 查询语句 */
	public final static String SELECT_SQL = "SELECT `Content` FROM `t_u_%s` WHERE `UserId` = %s;";
	/** 插入语句 */
	public final static String INSERT_SQL = "INSERT INTO `t_u_%s`(`UserId`,`ModuleId`,`Content`) VALUES(?,?,?);";
	/** 更新语句 */
	public final static String UPDATE_SQL = "UPDATE `t_u_%s` SET `Content` = ? WHERE `UserId` = ? and `ModuleId` = ?;";

	/** 玩家该模块数据(数据库用JSON存储) */
	protected Map<String, Map<String, Object>> dataMap = new ConcurrentHashMap<>();

	/** 改变的数据map 模板id-内容字段 */
	protected final Map<String, String> changeMap = new ConcurrentHashMap<>();

	/** 需要插入的数据map 模板id-操作 */
	protected final Map<String, Integer> sqlChangeMap = new ConcurrentHashMap<>();


	

	/**
	 * <pre>
	 * 设置数据为更新状态
	 * </pre>
	 */
	protected void setUpdate(String moduleId) {
		Integer op = sqlChangeMap.get(moduleId);
		if (op == null || op == JsonDataOption.None) {
			sqlChangeMap.put(moduleId, (int) JsonDataOption.Update);
		}
	}

	/**
	 * <pre>
	 * 设置数据为插入状态
	 * </pre>
	 */
	public void setInsert(String moduleId) {
		sqlChangeMap.put(moduleId, (int) JsonDataOption.Insert);
	}

	/**
	 * <pre>
	 * 增加到改变map
	 * </pre>
	 */
	public void addChangeMap(String moduleId, String key) {
		changeMap.put(moduleId, key);
	}

	/**
	 * @return
	 */
	public boolean load() {
		String selectSql = String.format(SELECT_SQL, moduleName, userId);
		Map<String, Map<String, Object>> tempMap = SQLExecutor.execSelectJSONMap(selectSql);
		if (tempMap != null) {
			dataMap.putAll(tempMap);
			tempMap.clear();
			tempMap = null;
			// String insertSql = String.format(INSERT_SQL, moduleName);
			// SQLExecutor.execInsertReturnId(insertSql, userId, "{}");
		}
		return true;
	}

	/**
	 */
	public void afterLoad() {
		// 初始化数据
		initData();
	}

	/**
	 * @return
	 */
	public boolean unload() {
		changeMap.clear();
		sqlChangeMap.clear();
		dataMap.clear();
		return true;
	}

	/**
	 * @return
	 */
	public boolean save() {
		if (dataMap.isEmpty()) {
			return true;
		}
		try {
			for (String moduleId : sqlChangeMap.keySet()) {
				Integer op = sqlChangeMap.get(moduleId);
				if (op == null) {
					continue;
				}
				try {
					if (op == JsonDataOption.Insert) {
						String insertSql = String.format(INSERT_SQL, moduleName);
						Map<String, Object> preMap = dataMap.get(moduleId);
						if (preMap == null) {
							preMap = new HashMap<>();
						}
						SQLExecutor.execInsert(insertSql, userId, moduleId, JsonUtil.stringify(preMap));
					} else if (op == JsonDataOption.Update) {
						String updateSql = String.format(UPDATE_SQL, moduleName);
						Map<String, Object> preMap = dataMap.get(moduleId);
						if (preMap == null) {
							preMap = new HashMap<>();
						}
						SQLExecutor.execUpdate(updateSql, JsonUtil.stringify(preMap), userId, moduleId);
					}
				} catch (Exception e) {
					Log.error("保存玩家数据失败,module: " + this.getClass().getSimpleName() + ", player: " + userId);
				}
			}
			sqlChangeMap.clear();
		} catch (Exception e) {
			Log.error("保存玩家数据失败,module: " + this.getClass().getSimpleName() + ", player: " + userId);
		}
		return true;
	}

	/**
	 * <pre>
	 * 初始化数据，只有数据本身为空时才设置
	 * </pre>
	 *
	 * @param key
	 * @param t
	 */
	public <T> void initData(String key, String moduleId, T t) {
		if (t == null) {
			Log.error("不能设置null值,key:" + key);
			return;
		}
		Map<String, Object> map = dataMap.get(key);
		if (map == null) {
			map = new HashMap<>();
			map.put(moduleId, t);
			dataMap.put(key, map);

			setInsert(moduleId);
		} else {
			Object pre = map.get(moduleId);
			if (pre == null) {
				map.put(key, t);
				dataMap.put(key, map);

				setInsert(moduleId);
			}
		}
	}

	/**
	 * <pre>
	 * 设置数据
	 * 获取时返回数据格式如下：
	 * 数字根据范围转化为int,long 浮点——>BigDecimal
	 * Date请用秒数或毫秒数，否则将返回yyyy-MM-dd HH:mm:ss类型字符串
	 * !为了提高性能、避免时区问题，建议牺牲一定可读性，使用int存储秒数表示Date
	 * </pre>
	 * 
	 * @param key
	 * @param t
	 */
	@Override
	public <T> void setData(String moduleId, String key, T t) {
		if (t == null) {
			// Log.error("不能设置null值,key:" + key);
			return;
		}
		Map<String, Object> jsonMap = dataMap.get(moduleId);
		if (jsonMap == null) {
			jsonMap = new HashMap<>();
			jsonMap.put(key, t);
			dataMap.put(moduleId, jsonMap);
			addChangeMap(moduleId, key);
			setInsert(moduleId);
		} else {
			Object pre = jsonMap.get(key);
			if (pre == null || !pre.equals(t) || (pre instanceof List) || (pre instanceof Map) ) {
				jsonMap.put(key, t);
				addChangeMap(moduleId, key);
				setUpdate(moduleId);
			}
		}
	}

	/**
	 * <pre>
	 * 获取数据
	 * 返回数据格式：
	 * 数字根据类型：int,long或BigDecimal(浮点)
	 * Date请用秒数或毫秒数，否则将返回yyyy-MM-dd HH:mm:ss类型字符串
	 * !为了提高性能、避免时区问题，建议牺牲一定可读性，使用int存储秒数表示Date
	 * </pre>
	 * 
	 * @param key
	 * @return
	 * @see com.hitalk.h5.base.module.able.Jsonable#getData(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final <T> T getData(String moduleId, String key) {
		Map<String, Object> jsonMap = dataMap.get(moduleId);
		if (jsonMap == null) {
			return null;
		} else {
			Object o = dataMap.get(key);
			if (o == null) {
				return null;
			}
			return (T) o;
		}
	}

	/**
	 * <pre>
	 * 获取整数数值
	 * 取JSON存储类型的值
	 * </pre>
	 * 
	 * @param key JSON的key
	 * @return
	 */
	public int getIntData(String moduleId, String key) {
		return MathUtil.getIntValue(dataMap, moduleId, key, this, userId);
	}

	/**
	 * @param key
	 * @param value
	 * @return 增加后的总值，返回0表示没有更新
	 */
	@Override
	public int addData(String moduleId, String key, int value) {
		if (value == 0) {
			return 0;
		}
		int preValue = MathUtil.getIntValue(dataMap, moduleId, key, this, userId);
		long newValue = preValue + value;
		if (newValue > Integer.MAX_VALUE) {
			Log.error("超出整型最大值,key:" + key + ",class:" + this.getClass().getSimpleName());
			newValue = Integer.MAX_VALUE;
		}
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put(key, (int) newValue);
		dataMap.put(moduleId, jsonMap);
		// 发送到客户端
		addChangeMap(moduleId, key);
		// 设置更新状态
		setUpdate(moduleId);
		return value;
	}
}
