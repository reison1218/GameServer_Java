/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.recharge.base.config;

/**
 * <pre>
 * Netty线程数配置
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public interface ThreadCount {

	/**
	 * <pre>
	 * 网关Netty线程数
	 * </pre>
	 * 
	 * @author reison
	 * @time 2019年7月27日
	 */
	public interface GateWayThreadCount {

		/** ws服务器线程数 */
		short WS_SERVER = 8;

		/** 连接游戏服客户端线程数 */
		short GAME_CLIENT = 4;

		/** 连接战斗服客户端线程数 */
		short ROOM_CLIENT = 8;

		/** http服务器线程数 */
		short HTTP_SERVER = 2;
	}

	/**
	 * <pre>
	 * 游戏服Netty线程数
	 * </pre>
	 * 
	 * @author reison
	 * @time 2019年7月27日
	 */
	public interface GameThreadCount {

		/** 游戏Socket服务器线程数 */
		short SOCKET_SERVER = 8;

		/** http服务器线程数 */
		short HTTP_SERVER = 8;

	}

	/**
	 * <pre>
	 * 房间服Netty线程数
	 * </pre>
	 * 
	 * @author reison
	 * @time 2019年7月27日
	 */
	public interface RoomThreadCount {

		/** 战斗Socket服务器线程数 */
		short SOCKET_SERVER = 8;

		/** http服务器线程数 */
		short HTTP_SERVER = 2;

	}

}
