/**
 * O * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.base.dao.SQLExecutor;
import com.base.entity.JsonDataOption;
import com.base.module.able.Jsonable;
import com.game.player.GamePlayer;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.MathUtil;

import io.netty.util.internal.ConcurrentSet;

/**
 * <pre>
 * 采用JSON格式存储数据的模块基类
 * </pre>
 * 
 * @author reison
 */
public abstract class JsonDataModule extends BaseModule implements Jsonable {
	

	/** 防止模块空，跳过保存，导致获取共享模块使用时，发生空指针异常 **/
	String BASE_KEY = "__i";

	/** 多JSON数据，无用的key标记，value为1时不加载进内存中，但是会存在于数据库 */
	String USELESS_KEY = "useless";

	/** 查询语句 */
	public final static String SELECT_SQL = "SELECT `Content` FROM `t_u_%s` WHERE `UserId` = %s;";
	/** 插入语句 */
	public final static String INSERT_SQL = "INSERT INTO `t_u_%s`(`UserId`,`Content`) VALUES(?,?);";
	/** 更新语句 */
	public final static String UPDATE_SQL = "UPDATE `t_u_%s` SET `Content` = ? WHERE `UserId` = ?;";

	/** 玩家该模块数据(数据库用JSON存储) */
	protected Map<String, Object> dataMap = new ConcurrentHashMap<>();

	/** 数据状态-详见Option */
	protected AtomicInteger option = new AtomicInteger();

	/** 改变的数据key列表 */
	protected final Set<String> changeList = new ConcurrentSet<>();

	public JsonDataModule(GamePlayer player) {
		super(player);
	}

	public JsonDataModule(int userId) {
		super(userId);
	}


	/**
	 * <pre>
	 * 初始化数据，只有数据本身为空时才设置
	 * </pre>
	 *
	 * @param key
	 * @param t
	 */
	public <T> void initData(String key, T t) {
		if (t == null) {
			Log.error("不能设置null值,key:" + key);
			return;
		}
		Object pre = dataMap.get(key);
		if (pre == null) {
			dataMap.put(key, t);
			setUpdate();
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
	public <T> void setData(String key, T t) {
		if (t == null) {
			// Log.error("不能设置null值,key:" + key);
			return;
		}
		Object pre = dataMap.get(key);
		if (pre == null || !pre.equals(t) || (pre instanceof List) || (pre instanceof Map)) {
			dataMap.put(key, t);
			addChangeList(key);
			setUpdate();
		}
	}

	/**
	 * @param key
	 * @param value
	 * @return 真正增加的值，返回0表示没有更新
	 */
	@Override
	public int addData(String key, int value) {
		if (value == 0) {
			return 0;
		}
		int preValue = MathUtil.getIntValue(dataMap, key, this, userId);
		long newValue = (long) preValue + value;
		if (newValue > Integer.MAX_VALUE) {
			Log.error("超出整型最大值,key:" + key + ",class:" + this.getClass().getSimpleName());
			newValue = Integer.MAX_VALUE;
		}
		dataMap.put(key, (int) newValue);
		// 发送到客户端
		addChangeList(key);
		// 设置更新状态
		setUpdate();
		return (int) newValue - preValue;
	}

	/**
	 * @param key
	 * @param value
	 * @return 真正增加的值，返回0表示没有更新
	 */
	public long addLongData(String key, long value) {
		if (value == 0L) {
			return 0L;
		}
		long preValue = MathUtil.getLongValue(dataMap, key, this, userId);
		long newValue = preValue + value;
		if (newValue < 0 && preValue > 0 && value > 0) {
			Log.error("超出Long最大值,key:" + key + ",preValue:" + preValue + ",value:" + value + ",newValue:" + newValue + ",class:" + this.getClass().getSimpleName());
			newValue = Long.MAX_VALUE;
		}
		dataMap.put(key, newValue);
		// 发送到客户端
		addChangeList(key);
		// 设置更新状态
		setUpdate();
		return newValue - preValue;
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
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		Object o = dataMap.get(key);
		if (o == null) {
			return null;
		}
		return (T) o;
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
	public int getIntData(String key) {
		return MathUtil.getIntValue(dataMap, key, this, userId);
	}

	/**
	 * <pre>
	 * 获取Long数值
	 * 取JSON存储类型的值
	 * </pre>
	 * 
	 * @param key JSON的key
	 * @return
	 */
	public long getLongData(String key) {
		return MathUtil.getLongValue(dataMap, key, this, userId);
	}
	
	/**
	 * <pre>
	 * 获取Double数值
	 * 取JSON存储类型的值
	 * </pre>
	 * 
	 * @param key JSON的key
	 * @return
	 */
	public double getDoubleData(String key) {
		return MathUtil.getDoubleValue(dataMap, key, this, userId);
	}

	/**
	 * @return
	 */
	public boolean load() {
		String selectSql = String.format(SELECT_SQL, moduleName, userId);
		Map<String, Object> tempMap = SQLExecutor.execSelectJSON(selectSql);
		// 新玩家，需新建数据
		if (tempMap != null) {
			dataMap.putAll(tempMap);
			tempMap.clear();
			tempMap = null;
		} else {
			// 防止模块空，跳过保存，导致获取共享模块使用时，发生空指针异常
			setData(BASE_KEY, 0);
			option.getAndSet(JsonDataOption.Insert);
			changeList.clear();
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
	 * @see com.hitalk.h5.game.module.able.Unloadable#unload()
	 */
	public boolean unload() {
		save();
		dataMap.clear();
		return true;
	}

	/**
	 * @return
	 * @see com.hitalk.h5.game.module.able.Savable#save()
	 */
	public boolean save() {
		if (dataMap.isEmpty()) {
			return true;
		}
		try {
			// 插入(使用状态预更新，否则高并发时会多次插入，主键重复)
			if (option.compareAndSet(JsonDataOption.Insert, JsonDataOption.None)) {
				String insertSql = String.format(INSERT_SQL, moduleName);
				if (SQLExecutor.execInsert(insertSql, userId, JsonUtil.stringify(dataMap)) == 0) {
					option.getAndSet(JsonDataOption.Insert);
					return false;
				}
			}
			// 更新
			else if (option.compareAndSet(JsonDataOption.Update, JsonDataOption.None)) {
				String updateSql = String.format(UPDATE_SQL, moduleName);
				if (!SQLExecutor.execUpdate(updateSql, JsonUtil.stringify(dataMap), userId)) {
					option.getAndSet(JsonDataOption.Update);
					return false;
				}
			}
		} catch (Throwable e) {
			Log.error("玩家Json模块数据保存异常,module:" + this.getClass().getSimpleName(), e);
			return false;
		}
		return true;
	}

	/**
	 * <pre>
	 * 覆盖整个dataMap
	 * </pre>
	 *
	 * @param tempMap
	 */
	public void updateDataMap(Map<String, Object> tempMap) {
		dataMap.clear();
		dataMap.putAll(tempMap);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " " + dataMap.toString();
	}

	/**
	 * <pre>
	 * 增加到改变列表
	 * </pre>
	 */
	public void addChangeList(String key) {
		changeList.add(key);
	}

	protected Map<String, Object> getChangePacket() {
		Map<String, Object> attrMap = new HashMap<>(16);
		for (String key : changeList) {
			attrMap.put(key, dataMap.get(key));
		}
		changeList.clear();
		return attrMap;
	}

	/**
	 * <pre>
	 * 设置数据为更新状态
	 * </pre>
	 */
	public void setUpdate() {
		option.compareAndSet(JsonDataOption.None, JsonDataOption.Update);
	}

	/**
	 * <pre>
	 * 设置数据为插入状态
	 * </pre>
	 */
	public void setInsert() {
		option.getAndSet(JsonDataOption.Insert);
	}

	public final Map<String, Object> getDataMap() {
		return dataMap;
	}

	/**
	 * <pre>
	 * 从Map中获取数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param mapKey JSON中Map的Key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final <T> T getMapData(String key, String mapKey) {
		Object map = dataMap.get(key);
		if (map == null) {
			return null;
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return null;
		}
		return (T) ((Map) map).get(mapKey);
	}

	/**
	 * <pre>
	 * 从Map中获取整型数据
	 * </pre>
	 *
	 * @param key
	 * @param mapKey
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final int getMapIntData(String key, String mapKey) {
		Object map = dataMap.get(key);
		if (map == null) {
			return 0;
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return 0;
		}
		return MathUtil.getIntValue((Map) map, mapKey, this, userId);
	}

	/**
	 * <pre>
	 * 从Map中移除数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param mapKey JSON中Map的Key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final <T> T removeMapData(String key, String mapKey) {
		Object map = dataMap.get(key);
		if (map == null) {
			return null;
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return null;
		}
		addChangeList(key);
		setUpdate();
		return (T) ((Map) map).remove(mapKey);
	}

	/**
	 * <pre>
	 * 移除JSON中某条数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @return
	 */
	// @SuppressWarnings("unchecked")
	// public final <T> T removeData(String key) {
	// Object o = dataMap.remove(key);
	// if (o == null) {
	// return null;
	// }
	// return (T) o;
	// }

	/**
	 * <pre>
	 * 往Map中设置数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param mapKey JSON中Map的Key
	 * @param t 需要设置的数据
	 */
	@SuppressWarnings("unchecked")
	public final <T> void setMapData(String key, String mapKey, T t) {
		Map<String, Object> map = (Map<String, Object>) dataMap.get(key);
		if (map == null) {
			map = new ConcurrentHashMap<>();
			dataMap.put(key, map);
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return;
		}
		if (!(t instanceof Number || t instanceof String || t instanceof Boolean || t instanceof Date)) {
			Log.error("JSON中Map数据只支持基本类型,key:" + key + ",mapKey:" + mapKey + ",data:" + t);
			return;
		}
		Object pre = map.get(mapKey);
		if (pre == null || !pre.equals(t)) {
			map.put(mapKey, t);
			addChangeList(key);
			setUpdate();
		}
		if (map.size() > 500) {
			Log.fatal("JSON中Map数据量太大,key:" + key + ",size:" + map.size());
		}
	}

	/**
	 * <pre>
	 * 往Map中增加数值
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param mapKey JSON中Map的Key
	 * @param value 需要增加的数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final int addMapData(String key, String mapKey, int value) {
		if (value == 0) {
			return 0;
		}
		Map<String, Object> map = (Map<String, Object>) dataMap.get(key);
		if (map == null) {
			map = new ConcurrentHashMap<>();
			dataMap.put(key, map);
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return 0;
		}
		int preValue = MathUtil.getIntValue(map, mapKey, this, userId);
		long newValue = (long) preValue + value;
		if (newValue > Integer.MAX_VALUE) {
			Log.error("超出整型最大值,key:" + key + ",mapKey:" + mapKey + ",class:" + this.getClass().getSimpleName());
			newValue = Integer.MAX_VALUE;
		}
		map.put(mapKey, (int) newValue);
		// 发送到客户端
		addChangeList(key);
		// 设置更新状态
		setUpdate();
		if (map.size() > 500) {
			Log.fatal("JSON中Map数据量太大,key:" + key + ",size:" + map.size());
		}
		return value;
	}

	/**
	 * <pre>
	 * 往Map中增加long数值
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param mapKey JSON中Map的Key
	 * @param value 需要增加的数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final long addLongMapData(String key, String mapKey, long value) {
		if (value == 0) {
			return 0;
		}
		Map<String, Object> map = (Map<String, Object>) dataMap.get(key);
		if (map == null) {
			map = new ConcurrentHashMap<>();
			dataMap.put(key, map);
		}
		if (!(map instanceof Map)) {
			Log.error("该元素不是Map,请检查该Key数据的存储,key:" + key);
			return 0;
		}
		long preValue = MathUtil.getLongValue(map, mapKey, this, userId);
		long newValue = preValue + value;
		if (newValue < 0 && preValue > 0 && value > 0) {
			Log.error("超出Long最大值,key:" + key + ",mapKey:" + mapKey + ",preValue:" + preValue + ",value:" + value + ",newValue:" + newValue + ",class:" + this.getClass().getSimpleName());
			newValue = Long.MAX_VALUE;
		}
		map.put(mapKey, newValue);
		// 发送到客户端
		addChangeList(key);
		// 设置更新状态
		setUpdate();
		if (map.size() > 500) {
			Log.fatal("JSON中Map数据量太大,key:" + key + ",size:" + map.size());
		}
		return value;
	}

	/**
	 * <pre>
	 * 从List中获取某一位整型数据
	 * </pre>
	 *
	 * @param key
	 * @param listIdx
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final int getListIntData(String key, int listIdx) {
		Object list = dataMap.get(key);
		if (list == null) {
			return 0;
		}
		if (!(list instanceof List)) {
			Log.error("该元素不是List,请检查该Key数据的存储,key:" + key);
			return 0;
		}
		return MathUtil.getIntValue((List) list, listIdx, this);
	}

	/**
	 * <pre>
	 * 往List中某一位设置数据
	 * ！谨慎调用该方法，如果有removeListData，容易产生索引错位或溢出
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param listIdx JSON中List的索引
	 * @param t 需要设置的数据
	 */
	@SuppressWarnings("unchecked")
	public final <T> void setListData(String key, int listIdx, T t) {
		if (t == null) {
			return;
		}
		List<Object> list = (List<Object>) dataMap.get(key);
		if (list == null) {
			list = new ArrayList<>();
			dataMap.put(key, list);
		}
		if (!(list instanceof List)) {
			Log.error("该元素不是List,请检查该Key数据的存储,key:" + key);
			return;
		}
		if (!(t instanceof Number || t instanceof String || t instanceof Boolean || t instanceof Date)) {
			Log.error("JSON中List数据只支持基本类型,key:" + key + ",listKey:" + listIdx + ",data:" + t);
			return;
		}
		if (listIdx < 0) {
			Log.error("超出List索引大小,key:" + key + ",listIdx:" + listIdx + ",size:" + list.size() + ",data:" + t);
			return;
		}
		if (listIdx > 500) {
			Log.error("数组超出长度限制，性能会较低,key:" + key + ",listIdx:" + listIdx + ",size:" + list.size() + ",data:" + t);
			return;
		}
		// 数组元素不够，补充元素
		if (listIdx > list.size() - 1) {
			for (int i = list.size(); i < listIdx + 1; i++) {
				list.add(0);
			}
		}
		Object pre = list.get(listIdx);
		if (pre == null || !pre.equals(t)) {
			list.set(listIdx, t);
			addChangeList(key);
			setUpdate();
		}
	}

	/**
	 * <pre>
	 * 往List中增加一条数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param t 需要增加的数据
	 */
	@SuppressWarnings("unchecked")
	public final <T> void putListData(String key, T t) {
		List<Object> list = (List<Object>) dataMap.get(key);
		if (list == null) {
			list = new ArrayList<>();
			dataMap.put(key, list);
		}
		if (!(list instanceof List)) {
			Log.error("该元素不是List,请检查该Key数据的存储,key:" + key);
			return;
		}
		if (!(t instanceof Number || t instanceof String || t instanceof Boolean || t instanceof Date)) {
			Log.error("JSON中List数据只支持基本类型,key:" + key + ",data:" + t);
			return;
		}
		list.add(t);
		addChangeList(key);
		setUpdate();
		if (list.size() > 500) {
			Log.fatal("JSON中List数据量太大,key:" + key + ",size:" + list.size());
		}
	}

	/**
	 * <pre>
	 * 往List中某一位增加数值
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param listIdx JSON中List的索引
	 * @param value 需要增加的数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final int addListData(String key, int listIdx, int value) {
		if (value == 0) {
			return 0;
		}
		List<Object> list = (List<Object>) dataMap.get(key);
		if (list == null) {
			list = new ArrayList<>();
			dataMap.put(key, list);
		}
		if (!(list instanceof List)) {
			Log.error("该元素不是List,请检查该Key数据的存储,key:" + key);
			return 0;
		}
		if (listIdx < 0) {
			Log.error("超出List索引大小,key:" + key + ",listIdx:" + listIdx + ",size:" + list.size() + ",value:" + value);
			return 0;
		}
		if (listIdx > 500) {
			Log.error("数组超出长度限制，性能会较低,key:" + key + ",listIdx:" + listIdx + ",size:" + list.size() + ",value:" + value);
			return 0;
		}
		// 数组元素不够，补充元素
		if (listIdx > list.size() - 1) {
			for (int i = list.size() - 1; i < listIdx; i++) {
				list.add(0);
			}
		}
		int preValue = MathUtil.getIntValue(list, listIdx, this);
		long newValue = (long) preValue + value;
		if (newValue > Integer.MAX_VALUE) {
			Log.error("超出整型最大值,key:" + key + ",listKey:" + listIdx + ",class:" + this.getClass().getSimpleName());
			newValue = Integer.MAX_VALUE;
		}
		list.set(listIdx, (int) newValue);
		// 发送到客户端
		addChangeList(key);
		// 设置更新状态
		setUpdate();
		return value;
	}

	/**
	 * <pre>
	 * 清除改变的key
	 * </pre>
	 */
	public void clearChangeList() {
		changeList.clear();
	}
}
