/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.json;

import java.util.Map;

import com.base.dao.SQLExecutor;
import com.base.entity.JsonDataOption;
import com.base.executor.ExecutorMgr;
import com.base.module.ModuleMgr;
import com.utils.JsonUtil;
import com.utils.Log;



/**
 * <pre>
 * 带DB操作的JSON数据
 * </pre>
 * 
 * @author reison
 * @time 2018年5月16日 下午9:34:42
 */
public abstract class BaseJsonDB extends BaseJsonData {

	/** 查询语句 */
	public final static String SELECT_SQL = "SELECT `Content` FROM `t_u_%s` WHERE `UserId` = %s;";
	/** 插入语句 */
	public final static String INSERT_SQL = "INSERT INTO `t_u_%s`(`UserId`,`Content`) VALUES(?,?);";
	/** 更新语句 */
	public final static String UPDATE_SQL = "UPDATE `t_u_%s` SET `Content` = ? WHERE `UserId` = ?;";

	/** 模块名称 */
	protected String moduleName;

	public BaseJsonDB(int id) {
		super(id, ExecutorMgr.getSceneExecutor());
		this.moduleName = ModuleMgr.getModuleName(this);
	}

	/**
	 * <pre>
	 * 从DB加载数据
	 * </pre>
	 *
	 * @return
	 */
	public boolean load() {
		String selectSql = String.format(SELECT_SQL, moduleName, id);
		Map<String, Object> tempMap = SQLExecutor.execSelectJSON(selectSql);
		// 新玩家，需新建数据
		if (tempMap != null) {
			dataMap.putAll(tempMap);
			tempMap.clear();
			tempMap = null;
		} else {
			option.getAndSet(JsonDataOption.Insert);
		}
		return true;
	}

	/**
	 * <pre>
	 * 卸载数据
	 * </pre>
	 *
	 * @return
	 */
	public boolean unload() {
		save();
		dataMap.clear();
		return true;
	}

	/**
	 * <pre>
	 * 保存DB数据
	 * </pre>
	 *
	 * @return
	 */
	public boolean save() {
		if (dataMap.isEmpty()) {
			return true;
		}
		try {
			// 插入(使用状态预更新，否则高并发时会多次插入，主键重复)
			if (option.compareAndSet(JsonDataOption.Insert, JsonDataOption.None)) {
				String insertSql = String.format(INSERT_SQL, moduleName);
				if (SQLExecutor.execInsert(insertSql, id, JsonUtil.stringify(dataMap)) == 0) {
					option.getAndSet(JsonDataOption.Insert);
				}
			}
			// 更新
			else if (option.compareAndSet(JsonDataOption.Update, JsonDataOption.None)) {
				String updateSql = String.format(UPDATE_SQL, moduleName);
				if (!SQLExecutor.execUpdate(updateSql, JsonUtil.stringify(dataMap), id)) {
					option.getAndSet(JsonDataOption.Update);
				}
			}
		} catch (Throwable e) {
			Log.error("玩家Json模块数据保存异常,module:" + this.getClass().getSimpleName(), e);
		}
		return true;
	}

}
