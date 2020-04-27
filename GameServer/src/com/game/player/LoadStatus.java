/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.player;

/**
 * <pre>
 * 登录加载数据 - 状态常量
 * </pre>
 * 
 * @author reison
 * @time 2018年3月6日 下午5:09:46
 */
public interface LoadStatus {

	/** 未加载状态 */
	int NOT_LOAD = 0;

	/** 正在加载状态 */
	int LOADING = 1;

	/** 已加载状态 */
	int LOADED = 2;

}
