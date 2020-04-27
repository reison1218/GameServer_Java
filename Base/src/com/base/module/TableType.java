/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.util.HashMap;
import java.util.Map;

import com.base.dao.SQLExecutor;


/**
 * <pre>
 *模块的存储格式，传统表、UserId+Json、UserId+TemplateId+Json.....
 * </pre>
 * 
 * @author reison
 * @time 2018年6月6日 上午9:34:42
 */
public enum TableType {
	
	/**
	 * 不是表结构，不用管
	 */
	NO_TABLE(1,null),
	/**
	 * json格式（UserId+json）单玩家单条数据
	 */
	JSON_TABLE_ONLY(2, SQLExecutor.TABLE_CREATE_SQL_SIMPLE),
	/**
	 * json格式（id+UserId+json）单玩家多条数据
	 */
	JSON_TABLE_MANY(3, SQLExecutor.TABLE_CREATE_SQL),
	/**
	 * Json格式（UserId+TemplateId+Json）单玩家多条数据，由模板id分
	 */
	JSON_TABLE_TEMP(4, SQLExecutor.TABLE_CREATE_SQL_TEMP),

	;

	private int type;
	private String sql = "";
	public static Map<Integer, String> tablePool = new HashMap<Integer, String>();

	private TableType(int type) {
		this.type = type;
	}

	private TableType(int type, String sql) {
		this.type = type;
		this.sql = sql;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public static String getSQLbyTableType(int type) {
		if (tablePool.isEmpty()) {
			for (TableType tt : TableType.values()) {
				tablePool.put(tt.type, tt.sql);
			}
		}
		return tablePool.get(type);
	}

	public static TableType getTableType(int type) {
		for (TableType tt : TableType.values()) {
			if (tt.type == type) {
				return tt;
			}
		}
		return null;
	}

	public boolean compare(TableType tableType) {
		if (this.type == tableType.getType()) {
			return true;
		} else {
			return false;
		}
	}

}
