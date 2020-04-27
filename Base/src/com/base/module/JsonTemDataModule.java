/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;
import com.base.dao.SQLExecutor;
import com.base.entity.JsonDataOption;
import com.base.module.able.JsonTemable;
import com.game.player.GamePlayer;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.MathUtil;

import io.netty.util.internal.ConcurrentSet;

/**
 * <pre>
 * 采用json存储-UserId+Tid为主键
 * 数据格式：
 * {
 *  tid1:{oneJson},
 *  tid2:{oneJson}
 * }
 * </pre>
 * 
 * @author reison
 */
public abstract class JsonTemDataModule extends BaseModule implements JsonTemable {

	/** 查询语句 */	
	public final static String SELECT_SQL = "SELECT `Tid`, `Content` FROM `t_u_%s` WHERE `UserId` = %s  AND (!JSON_CONTAINS_PATH(`Content`,'one','$.useless') OR json_extract(`Content`,'$.useless' )=0);";
	/** 插入语句 */
	public final static String INSERT_SQL = "REPLACE INTO `t_u_%s` (`UserId`,`Tid`,`Content`) VALUES(?,?,?);";

	/** 玩家该模块数据(数据库用JSON存储) */
	protected Map<Integer, JSONObject> dataTemMap = new ConcurrentHashMap<Integer, JSONObject>();

	/** 改变的数据key列表 返回给客户端 */
	protected final Map<Integer, Set<String>> changeMapSet = new ConcurrentHashMap<Integer, Set<String>>();

	/** 玩家游戏中更改的需要存进数据库的数据，保存之后remove */
	protected Set<Integer> saveTemSet = new ConcurrentSet<Integer>();

	/** 数据状态-详见Option */
	protected AtomicInteger option = new AtomicInteger();

	public JsonTemDataModule(GamePlayer player) {
		super(player);
	}

	public JsonTemDataModule(int userId) {
		super(userId);
	}

	/**
	 * @return
	 */
	@Override
	public boolean load() {
		String selectSql = String.format(SELECT_SQL, moduleName, userId);
		Map<Integer, JSONObject> tempJsonMap = SQLExecutor.execSelectTemJSON(selectSql);
		// 新玩家，需新建数据
		if (tempJsonMap != null) {
			dataTemMap.putAll(tempJsonMap);
			tempJsonMap.clear();
			tempJsonMap = null;
		} else {
			// 现在新版的json 不需要插入空数据了 下面注释掉
			option.getAndSet(JsonDataOption.Insert);
		}
		return true;
	}

	/**
	 */
	@Override
	public void afterLoad() {
		// 初始化数据
		initData();
	}

	/**
	 * @return
	 */
	@Override
	public boolean unload() {
		save();
		dataTemMap.clear();
		return false;
	}

	/**
	 * @return
	 */
	@Override
	public boolean save() {
		if (dataTemMap.isEmpty()) {
			return true;
		}
		try {
			// 插入(使用状态预更新，否则高并发时会多次插入，主键重复)
			if (option.compareAndSet(JsonDataOption.Insert, JsonDataOption.None)) {
				String insertSql = String.format(INSERT_SQL, moduleName);
				for (Integer tid : dataTemMap.keySet()) {
					if (SQLExecutor.execInsert(insertSql, userId, tid, JsonUtil.stringify(dataTemMap.get(tid))) == 0) {
						option.getAndSet(JsonDataOption.Insert);
					}
				}
			}
			// 更新
			else if (option.compareAndSet(JsonDataOption.Update, JsonDataOption.None)) {
				String updateSql = String.format(INSERT_SQL, moduleName);
				Iterator<Integer> iterator = saveTemSet.iterator();
				while (iterator.hasNext()) {
					Integer tid = iterator.next();
					if (SQLExecutor.execInsert(updateSql, userId, tid, JsonUtil.stringify(dataTemMap.get(tid))) == 0) {
						option.getAndSet(JsonDataOption.Update);
					} else {
						iterator.remove();
					}
				}
			}
		} catch (Throwable e) {
			Log.error("玩家Json模块数据保存异常,module:" + this.getClass().getSimpleName(), e);
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
	public void updateDataMap(Map<Integer, JSONObject> tempMap) {
		dataTemMap.clear();
		dataTemMap.putAll(tempMap);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " " + JsonUtil.stringify(dataTemMap);
	}

	// ---------------------------------------------------
	/**
	 * @param tid
	 * @param key
	 * @param t
	 * @see com.hitalk.h5.base.module.able.JsonTemable#setData(int,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public <T> void setData(int tid, String key, T t) {
		if (t == null) {
			Log.error("value不能设置为null。TemplateId:" + tid + "-key:" + key);
			return;
		}
		JSONObject map = dataTemMap.get(tid);
		if (map == null || map.isEmpty()) {
			map = new JSONObject();
			dataTemMap.put(tid, map);
		}
		Object value = map.get(key);
		if (value == null || !value.equals(t) || (value instanceof List) || (value instanceof Map)) {
			// 写入数据
			map.put(key, t);
			// 需要返回给客户端的数据key
			addChangeMapSet(tid, key);
			// 记录等待保存的templateId
			addWaitUpdataTemSet(tid);
			// 设置为更新状态 等待内存写入数据库
			setUpdate();
		}
	}

	/**
	 * 将key插入某个模板中的map
	 * 
	 * @param tid
	 * @param key
	 * @param value
	 * @return 增加后的总值，返回0表示没有更新
	 * @see com.hitalk.h5.base.module.able.JsonTemable#addData(int,
	 *      java.lang.String, int)
	 */
	@Override
	public long addData(int tid, String key, long value) {
		if (value == 0L) {
			return 0L;
		}
		JSONObject map = dataTemMap.get(tid);
		if (map == null) {
			map = new JSONObject();
			dataTemMap.put(tid, map);
		}
		long preValue = MathUtil.getLongValue(map, key, this, userId);
		long newValue = preValue + value;
		if (newValue < 0 && preValue > 0 && value > 0) {
			Log.error("超出Long最大值,key:" + key + ",preValue:" + preValue + ",value:" + value + ",newValue:" + newValue
					+ ",class:" + this.getClass().getSimpleName());
			newValue = Long.MAX_VALUE;
		}
		map.put(key, newValue);
		// 发送到客户端
		addChangeMapSet(tid, key);
		// 记录等待保存的templateId
		addWaitUpdataTemSet(tid);
		// 设置更新状态
		setUpdate();
		return newValue;
	}

	/**
	 * <pre>
	 * 将json 覆盖掉某个模板中的Content
	 * </pre>
	 *
	 * @param tid
	 * @param jsonObj
	 * @return false表示覆盖失败 true表示覆盖成功
	 */
	public boolean setData(int tid, JSONObject jsonObj) {
		if (jsonObj == null) {
			return false;
		}
		JSONObject map = dataTemMap.get(tid);
		if (map != null) {
			Log.error("添加整条json，发现已存在,tid:" + tid);
			return false;
		}
		map = new JSONObject(jsonObj);
		dataTemMap.put(tid, map);
		// 发送到客户端
		addChangeMapSet(tid);
		// 记录等待保存的templateId
		addWaitUpdataTemSet(tid);
		// 设置更新状态
		setUpdate();
		return true;
	}

	/**
	 * @param tid
	 * @param key
	 * @return
	 * @see com.hitalk.h5.base.module.able.JsonTemable#getData(int,
	 *      java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getData(int tid, String key) {
		JSONObject map = dataTemMap.get(tid);
		if (map == null) {
			return null;
		}
		Object o = map.get(key);
		if (o == null) {
			return null;
		}
		return (T) o;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(int tid) {
		Object o = dataTemMap.get(tid);
		if (o == null) {
			return null;
		}
		return (T) o;
	}

	/**
	 * @param tid
	 * @param key
	 * @return
	 */
	@Override
	public int getIntData(int tid, String key) {
		JSONObject map = dataTemMap.get(tid);
		if (map == null) {
			return 0;
		}
		return MathUtil.getIntValue(map, key, this, userId);
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
	public long getLongData(int tid, String key) {
		JSONObject map = dataTemMap.get(tid);
		if (map == null) {
			return 0l;
		}
		return MathUtil.getLongValue(map, key, this, userId);
	}

	/**
	 * <pre>
	 * 添加修改过的tem key，返回给前端 和 用来更新单个字段
	 * </pre>
	 *
	 * @param tid
	 * @param key
	 */
	private void addChangeMapSet(int tid, String key) {
		Set<String> set = changeMapSet.get(tid);
		if (set == null) {
			set = new HashSet<String>();
			changeMapSet.put(tid, set);
		}
		set.add(key);
	}

	/**
	 * <pre>
	 * 添加修改过的tem 和所有的Key，返回给前端 和 用来更新单个字段
	 * </pre>
	 *
	 * @param tid
	 * @param key
	 */
	protected void addChangeMapSet(int tid) {
		Set<String> set = changeMapSet.get(tid);
		if (set == null) {
			set = new HashSet<String>();
			changeMapSet.put(tid, set);
		}
		set.clear();
		Map<String, Object> map = dataTemMap.get(tid);
		if (map != null && !map.isEmpty()) {
			set.addAll(map.keySet());
		}
	}

	/**
	 * <pre>
	 * 将修改的数据发送给客户端， 客户端不需要的数据 请在子类自行重写该方法
	 * </pre>
	 *
	 * @return
	 */
	public Map<Integer, Map<String, Object>> getChangePacket() {
		Map<Integer, Map<String, Object>> packet = new ConcurrentHashMap<Integer, Map<String, Object>>();
		for (Entry<Integer, Set<String>> entry : changeMapSet.entrySet()) {
			int tid = entry.getKey();
			Set<String> changeKeys = entry.getValue();
			Map<String, Object> oneJson = new ConcurrentHashMap<>();
			packet.put(tid, oneJson);
			for (String changeKey : changeKeys) {
				oneJson.put(changeKey, getData(tid, changeKey));
			}
		}
		changeMapSet.clear();
		return packet;
	}

	/**
	 * <pre>
	 * 清除改变的key，只在共享数据里面才调用。
	 * </pre>
	 */
	public void clearChangeMapSet() {
		changeMapSet.clear();
	}

	/**
	 * <pre>
	 * 所有的数据发送给客户端， 客户端不需要的数据 请在子类自行重写该方法
	 * </pre>
	 *
	 * @return
	 */
	public Map<Integer, Map<String, Object>> getAllPacket() {
		Map<Integer, Map<String, Object>> pack = new ConcurrentHashMap<Integer, Map<String, Object>>(dataTemMap);
		return pack;
	}

	/**
	 * <pre>
	 * 得到单条的数据
	 * </pre>
	 *
	 * @return
	 */
	public Map<String, Object> getOneDataPacket(int onlyId) {
		Map<String, Object> pack = new ConcurrentHashMap<String, Object>();
		Map<String, Object> map = dataTemMap.get(onlyId);
		pack.putAll(map);
		return pack;
	}

	/**
	 * <pre>
	 * 添加修改过的tem，等待保存进数据库
	 * </pre>
	 *
	 * @param tid
	 * @param key
	 */
	private void addWaitUpdataTemSet(int tid) {
		saveTemSet.add(tid);
	}

	/**
	 * <pre>
	 * 设置数据为更新状态
	 * </pre>
	 */
	private void setUpdate() {
		option.compareAndSet(JsonDataOption.None, JsonDataOption.Update);
	}

	/**
	 * <pre>
	 * 得到所有数据
	 * </pre>
	 *
	 * @return
	 */
	public Map<Integer, JSONObject> getAllData() {
		return dataTemMap;
	}
}
