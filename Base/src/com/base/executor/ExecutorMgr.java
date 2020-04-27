/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.executor;

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

	/** 邮件发送执行器-用于异步发送邮件-使用默认队列 -！！！该执行器仅用于发送邮件 */
	private final static CommonExecutor defaultMailExecutor = new CommonExecutor(1, "DefaultMail");

	/** 缓慢Action执行器 - 主要用于处理不是特别重要的逻辑-使用功能模块的队列，目前用于ip坐标获取 **/
	private final static CommonExecutor slowExecutor = new CommonExecutor(1, "other");

	/** 玩家Action执行器 -主要用于接收到客户端包的逻辑处理-使用玩家队列 */
	private final static CommonExecutor playerExecutor = new CommonExecutor(8, "Player");

	/** 非玩家类Action执行器-主要用于服务器之间通讯-使用功能模块的队列 */
	private final static CommonExecutor serverExecutor = new CommonExecutor(8, "Server");

	/** 广播Action执行器 - 聊天等广播逻辑-使用功能模块的队列 */
	private final static CommonExecutor broadExecutor = new CommonExecutor(8, "Broad");

	/** 场景Action执行器 - 主要用于场景相关逻辑-使用功能模块的队列 */
	private final static CommonExecutor sceneExecutor = new CommonExecutor(8, "Scene");

	/** 队伍Action执行器 - 主要用于队伍相关逻辑-使用功能模块的队列 */
	private final static CommonExecutor teamExecutor = new CommonExecutor(8, "Team");

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
	
	public static void stop() {
		defaultExecutor.getExecutor().stop();
		slowExecutor.getExecutor().stop();
		playerExecutor.getExecutor().stop();
		serverExecutor.getExecutor().stop();
		broadExecutor.getExecutor().stop();
		sceneExecutor.getExecutor().stop();
		teamExecutor.getExecutor().stop();
	}

	/**
	 * <pre>
	 * ！！！该执行器仅用于发送邮件
	 * </pre>
	 *
	 * @return
	 */
	public static final CommonExecutor getDefaultMailExecutor() {
		return defaultMailExecutor;
	}

	public static final CommonExecutor getPlayerExecutor() {
		return playerExecutor;
	}

	public static final CommonExecutor getServerExecutor() {
		return serverExecutor;
	}

	public static final CommonExecutor getBroadExecutor() {
		return broadExecutor;
	}

	public static final CommonExecutor getSceneExecutor() {
		return sceneExecutor;
	}

	public static final CommonExecutor getTeamExecutor() {
		return teamExecutor;
	}

	public static CommonExecutor getSlowExecutor() {
		return slowExecutor;
	}

}
