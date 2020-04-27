package com.base.entity;

/**
 * <pre>
 * JSON缓存数据状态
 * </pre>
 * 
 * @author reison
 * @time 2017年3月20日 上午10:52:26
 */
public interface JsonDataOption {

	/**
	 * 无状态
	 */
	short None = 0;

	/**
	 * 插入
	 */
	short Insert = 1;

	/**
	 * 更新
	 */
	short Update = 2;

}
