/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.bridge;

import java.util.List;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.config.ThreadCount.GateWayThreadCount;
import com.base.netty.BaseNettyClient;
import com.base.netty.packet.Packet;
import com.base.netty.socketclient.SocketClient;
import com.base.netty.util.MessageUtil;
import com.gate.netty.handler.SocketClientHandler;
import com.gate.netty.handler.SocketClientInitializer;
import com.utils.Log;
import com.utils.RandomUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * <pre>
 * Socket连接管理
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class ChannelMgr {

	/** 游戏服客户端连接 */
	private static BaseNettyClient gameClient = null;

	/** 房间服服客户端连接 */
	private static BaseNettyClient roomClient = null;

	/** 是否需重连游戏服 */
	private static boolean needReconnGame = false;

	/** 是否需重连游戏服 */
	private static boolean needReconnRoom = false;

	private ChannelMgr() {
	}

	/**
	 * <pre>
	 * 随机分配一连接发送数据包
	 * </pre>
	 *
	 * @param body
	 * @param chs
	 * @return
	 */
	private static final Channel selChannel(BaseNettyClient client, boolean needReconn) {
		if (client == null) {
			return null;
		}
		List<Channel> chs = client.getChs();
		if (chs == null || chs.isEmpty()) {
			return null;
		}
		int randIdx = RandomUtil.rand(chs.size());
		Channel conn = chs.get(randIdx);
		// 检测连接
		if (!isActive(conn)) {
			if (needReconn) {
				// 重连服务器
				conn = client.reconnect(randIdx);
				if (!isActive(conn)) {
					return null;
				}
			}
		}
		return conn;
	}

	/**
	 * <pre>
	 * 构建数据包
	 * </pre>
	 *
	 * @param conn
	 * @param code
	 * @param body
	 * @param params
	 * @return
	 */
	private static final ByteBuf buildBuf(Channel conn, int code, Object body, int... params) {
		ByteBuf buf = null;
		// 数据包是否来自客户端
		if (params.length > 0 && params[0] > 0) {
			buf = MessageUtil.buildBuf(code, body, conn, params[0], true, false, null);
		} else {
			buf = MessageUtil.buildServerBuf(code, body, conn);
		}
		return buf;
	}

	/**
	 * <pre>
	 * 发送到游戏服
	 * </pre>
	 *
	 * @param code
	 * @param body
	 * @param params
	 */
	public static final void send2Game(short code, Object body, int... params) {
		Channel ch = selChannel(gameClient, needReconnGame);
		if (ch == null) {
			return;
		}
		MessageUtil.writeChannel(buildBuf(ch, code, body, params), ch);
	}

	/**
	 * <pre>
	 * 发送到大跨服1
	 * </pre>
	 *
	 * @param code
	 * @param body
	 * @param params
	 */
	public static final void send2Room(int code, Object body, int... params) {
		Channel ch = selChannel(roomClient, needReconnRoom);
		if (ch == null) {
			return;
		}
		MessageUtil.writeChannel(buildBuf(ch, code, body, params), ch);
	}

	/**
	 * <pre>
	 * 发送到游戏服
	 * </pre>
	 *
	 * @param packet
	 */
	public static final void send2Game(Packet packet) {
		Channel conn = selChannel(gameClient, needReconnGame);
		if (conn == null || !conn.isActive()) {
			Log.error("Channel is null or not active!");
			return;
		}

		ByteBuf buf = MessageUtil.buildBufByPacket(conn, packet);
		MessageUtil.writeChannel(buf, conn);
	}

	/**
	 * <pre>
	 * 发送到房间服
	 * </pre>
	 *
	 * @param packet
	 */
	public static final void send2Room(Packet packet) {
		Channel conn = selChannel(roomClient, needReconnRoom);
		if (conn == null) {
			return;
		}
		ByteBuf buf = MessageUtil.buildBufByPacket(conn, packet);
		MessageUtil.writeChannel(buf, conn);
	}

	/**
	 * <pre>
	 * 连接游戏服
	 * </pre>
	 *
	 * @return
	 */
	public static final boolean connectGame() {
		String host = Config.getConfig(ConfigKey.GAME_SERVER);
		int port = Config.getIntConfig(ConfigKey.GAME_PORT);
		BaseNettyClient client = BaseNettyClient.createInstance(SocketClient.class, "GameServer", host, port,
				GateWayThreadCount.GAME_CLIENT, new SocketClientInitializer(new SocketClientHandler()));
		if (client == null) {
			return false;
		}
		gameClient = client;
		if (gameClient.getChs().isEmpty() || !isActive(gameClient.getChs().get(0))) {
			Log.error("连接游戏服错误,host:" + host + ",port:" + port);
			return false;
		}
		Log.info("游戏服连接成功！");
		needReconnGame = false;
		return true;
	}

	/**
	 * <pre>
	 * 连接room服
	 * </pre>
	 *
	 * @return
	 */
	public static final boolean connectRoom() {
		String host = Config.getConfig(ConfigKey.ROOM_SERVER);
		int port = Config.getIntConfig(ConfigKey.ROOM_PORT);
		BaseNettyClient client = BaseNettyClient.createInstance(SocketClient.class, "RoomServer", host, port,
				GateWayThreadCount.ROOM_CLIENT, new SocketClientInitializer(new SocketClientHandler()));
		if (client == null) {
			return false;
		}
		roomClient = client;
		if (roomClient.getChs().isEmpty() || !isActive(roomClient.getChs().get(0))) {
			Log.error("连接房间服错误,host:" + host + ",port:" + port);
			return false;
		}
		Log.info("房间服连接成功！");
		needReconnRoom = false;
		return true;
	}

	/**
	 * <pre>
	 * 连接是否有效
	 * </pre>
	 *
	 * @param channel
	 * @return
	 */
	private static final boolean isActive(Channel channel) {
		return channel != null && channel.isOpen() && channel.isActive();
	}

	public static boolean reconnRoom() {
		return roomClient.reconnect();
	}

	public static boolean reconnGame() {
		return gameClient.reconnect();
	}

}
