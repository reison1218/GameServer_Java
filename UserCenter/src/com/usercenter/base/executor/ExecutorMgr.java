/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.usercenter.base.executor;

import com.utils.Log;

/**
 * <pre>
 * 执行器管理
 * </pre>
 * 
 * @author reison
 */
public final class ExecutorMgr {

	/** 默认执行器-用于本身无队列的逻辑执行-使用默认队列 */
	private final static CommonExecutor defaultExecutor = new CommonExecutor(1, "Default");

	/** 缓慢Action执行器 - 主要用于处理不是特别重要的逻辑-使用功能模块的队列，目前用于ip坐标获取 **/
	private final static CommonExecutor slowExecutor = new CommonExecutor(1, "other");

	/** 玩家Action执行器 -主要用于接收到客户端包的逻辑处理-使用玩家队列 */
	private final static CommonExecutor orderExecutor = new CommonExecutor(8, "Order");


	private ExecutorMgr() {
	}

	/**
	 * <pre>
	 * 预启动线程
	 * </pre>
	 *
	 * @return
	 */
	public static final boolean init() {
		Log.info("线程池初始化成功~");
		return true;
	}

	public static final CommonExecutor getDefaultExecutor() {
		return defaultExecutor;
	}

	public static final CommonExecutor getOrderExecutor() {
		return orderExecutor;
	}


	public static CommonExecutor getSlowExecutor() {
		return slowExecutor;
	}

}
