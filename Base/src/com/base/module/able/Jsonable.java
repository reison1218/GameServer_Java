/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

/**
 * <pre>
 * 模块数据JSON化
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日 
 */
public interface Jsonable {

	/**
	 * <pre>
	 * 字段初始化默认值
	 * </pre>
	 */
	void initData();

	/**
	 * <pre>
	 * 设置模块某数据
	 * </pre>
	 *
	 * @param key
	 * @param t
	 */
	<T> void setData(String key, T t);

	/**
	 * <pre>
	 * 增加模块某数值
	 * !增加后的总值不能超整型最大值
	 * </pre>
	 *
	 * @param key
	 * @param value
	 * @return 增加后的总值，返回0表示没有更新
	 */
	int addData(String key, int value);
	


	/**
	 * <pre>
	 * 获取模块某数据
	 * </pre>
	 *
	 * @param key
	 */
	<T> T getData(String key);

	/**
	 * <pre>
	 * 获取整型数据
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	int getIntData(String key);
}
