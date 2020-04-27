/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.player;


/**
 * <pre>
 * 玩家接口
 * </pre>
 * 
 * @author reison
 * @time 2017年3月20日 上午10:14:18
 */
public interface IPlayer {

	/**
	 * <pre>
	 * 获取用户Id
	 * </pre>
	 *
	 * @return
	 */
	int getUserId();

	/**
	 * <pre>
	 * 获取模块
	 * </pre>
	 *
	 * @param moduleClass
	 * @return
	 */
	<T> T getModule(Class<T> moduleClass);

}
