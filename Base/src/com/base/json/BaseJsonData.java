/**
 * O * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.base.entity.JsonDataOption;
import com.base.executor.AbstractActionQueue;
import com.base.executor.CommonExecutor;
import com.base.module.IModule;
import com.utils.Log;
import com.utils.MathUtil;

import io.netty.util.internal.ConcurrentSet;

/**
 * <pre>
 * JSON数据基础容器
 * </pre>
 * 
 * @author reison
 * @time 2018年5月16日 下午8:38:45
 */
public abstract class BaseJsonData extends AbstractActionQueue implements IModule {

	/** 玩家该模块数据(数据库用JSON存储) */
	protected Map<String, Object> dataMap = new ConcurrentHashMap<>();

	/** 改变的数据key列表 */
	protected final Set<String> changeList = new ConcurrentSet<>();

	/** 数据状态-详见Option */
	protected AtomicInteger option = new AtomicInteger();

	/** 玩家UserId、怪物模板Id、节点模板Id等惟一Id */
	protected int id;

	/**
	 * 需要执行器的构造方法
	 */
	public BaseJsonData(int id, CommonExecutor executor) {
		super(executor.getExecutor());
		this.id = id;
	}

	/**
	 * 不需要执行器的构造方法
	 */
	public BaseJsonData(int id) {
		super(null);
		this.id = id;
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
	 * @param key
	 * @param value
	 * @return 增加后的总值，返回0表示没有更新
	 */
	public int addData(String key, int value) {
		if (value == 0) {
			return 0;
		}
		int preValue = MathUtil.getIntValue(dataMap, key, this, id);
		long newValue = preValue + value;
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
	 * @return 增加后的总值，返回0表示没有更新
	 */
	public long addLongData(String key, long value) {
		if (value == 0L) {
			return 0L;
		}
		long preValue = MathUtil.getLongValue(dataMap, key, this, id);
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
		return value;
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
	@SuppressWarnings("unchecked")
	public final <T> T getData(String key) {
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
		return MathUtil.getIntValue(dataMap, key, this, id);
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
		return MathUtil.getLongValue(dataMap, key, this, id);
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
			attrMap.put(key, getData(key));
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
		return MathUtil.getIntValue((Map) map, mapKey, this, id);
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
		int preValue = MathUtil.getIntValue(map, mapKey, this, id);
		long newValue = preValue + value;
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
	 * 从List中获取某一位数据
	 * </pre>
	 *
	 * @param key JSON的key
	 * @param listIdx JSON中List的索引
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final <T> T getListData(String key, int listIdx) {
		Object list = dataMap.get(key);
		if (list == null) {
			return null;
		}
		if (!(list instanceof List)) {
			Log.error("该元素不是List,请检查该Key数据的存储,key:" + key);
			return null;
		}
		return (T) ((List) list).get(listIdx);
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
		if (listIdx < 0 || listIdx > list.size() - 1) {
			Log.error("超出List索引大小,key:" + key + ",listKey:" + listIdx + ",size:" + list.size() + ",data:" + t);
			return;
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
		if (listIdx < 0 || listIdx > list.size() - 1) {
			Log.error("超出List索引大小,key:" + key + ",listKey:" + listIdx + ",size:" + list.size() + ",value:" + value);
			return 0;
		}
		int preValue = MathUtil.getIntValue(list, listIdx, this);
		long newValue = preValue + value;
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

	public int getId() {
		return id;
	}

}
