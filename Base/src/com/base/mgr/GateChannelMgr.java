/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.base.config.ConfigKey;
import com.base.netty.handler.SocketServerHandler;
import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketDesc;
import com.base.netty.packet.PacketKey;
import com.base.netty.util.MessageUtil;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.RandomUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * <pre>
 * 网关连接管理
 * ·-包含连接到本进程的所有网关
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class GateChannelMgr {

	/** 网关连接缓存<serverId,Channel> */
	private final static Map<Integer, List<Channel>> gateChannels = new ConcurrentHashMap<>();

	/** 玩家区服缓存<userId,serverId> */
	private final static Map<Integer, Integer> user2Servers = new ConcurrentHashMap<>();

	/** 玩家连接缓存<userId,Channel> */
	private final static Map<Integer, Channel> user2Channels = new ConcurrentHashMap<>();

	/**
	 * <pre>
	 * 增加网关连接
	 * </pre>
	 *
	 * @param chJsonObj
	 * @param channel
	 */
	public final static void addChannel(JSONObject chJsonObj, Channel channel) {
		int serverId = chJsonObj.getIntValue(ConfigKey.SERVER_ID);
		channel.attr(SocketServerHandler.GATE_KEY).set(serverId);
		List<Channel> list = gateChannels.get(serverId);
		if (list == null) {
			list = new ArrayList<>();
			gateChannels.put(serverId, list);
		}
		list.add(channel);
		Log.info("注册网关连接，serverId：" + serverId + ",channel:" + channel.remoteAddress());
		// 注册网关上已有玩家
		@SuppressWarnings("unchecked")
		List<Integer> uidArr = (List<Integer>) chJsonObj.get(PacketKey.USERS_REG);
		if (uidArr == null || uidArr.isEmpty()) {
			return;
		}
		for (int uid : uidArr) {
			bindUserServer(uid, serverId);
		}
	}

	/**
	 * <pre>
	 * 移除连接
	 * </pre>
	 *
	 * @param serverId
	 * @param channel
	 */
	public final static void removeChannel(int serverId, Channel channel) {
		List<Channel> list = gateChannels.get(serverId);
		if (list != null && !list.isEmpty()) {
			if (list.remove(channel)) {
				// 断开连接时，移除所有的绑定关系
				removeBindCache(channel);
				Log.info("移除网关连接,serverId:" + serverId + ",ch:" + channel);
			}
		}
	}

	/**
	 * <pre>
	 * 移除连接绑定关系
	 * </pre>
	 *
	 * @param channel
	 */
	private final static void removeBindCache(Channel channel) {
		Integer serverId = channel.attr(SocketServerHandler.GATE_KEY).get();
		if (serverId == null) {
			return;
		}
		for (Iterator<Entry<Integer, Integer>> it = user2Servers.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Integer> entry = it.next();
			int sid = entry.getValue();
			if (serverId == sid) {
				it.remove();
			}
		}
		for (Iterator<Entry<Integer, Channel>> it = user2Channels.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Channel> entry = it.next();
			Channel ch = entry.getValue();
			if (channel == ch) {
				it.remove();
			}
		}
	}

	/**
	 * <pre>
	 * 获取每个网关一个连接的集合
	 * </pre>
	 *
	 * @return
	 */
	private final static List<Channel> getAllGateChs() {
		List<Channel> result = new ArrayList<>(gateChannels.size());
		for (List<Channel> list : gateChannels.values()) {
			if (list == null || list.isEmpty()) {
				continue;
			}
			result.add(list.get(RandomUtil.rand(list.size())));
		}
		return result;
	}

	/**
	 * <pre>
	 * 广播客户端数据包
	 * ·-发送到多玩家玩家区服的网关
	 * ·-然后强制转发到客户端
	 * </pre>
	 *
	 * @param code      协议号
	 * @param body      需广播的数据包
	 * @param broadUids 需要广播的用户Id(不能和其他引用关联, 因为会被clear)
	 */
	public final static void broadClientPacket(short code, Object body, Collection<Integer> broadUids) {
		broadClientPacket(code, body, broadUids, false);
	}

	/**
	 * <pre>
	 * 广播客户端数据包
	 * ·-发送到多玩家区服的网关
	 * ·-然后强制转发到客户端
	 * </pre>
	 *
	 * @param code          协议号
	 * @param body          需广播的数据包
	 * @param broadUids     需要广播的用户Id(不能和其他引用关联, 因为会被clear)
	 * @param canDiscardPkg 负载过高时，是否可丢弃一些包
	 */
	public final static void broadClientPacket(short code, Object body, Collection<Integer> broadUids,
			boolean canDiscardPkg) {
		if (broadUids == null || broadUids.isEmpty()) {
			return;
		}
		List<Channel> chs = getChannel(broadUids);
		if (chs == null || chs.isEmpty()) {
			return;
		}
		ByteBuf buf = MessageUtil.buildBroadAllBuf(code, body, chs.get(0), broadUids);
		MessageUtil.writeChannel(buf, chs, canDiscardPkg);
	}

	/**
	 * <pre>
	 * 发送单玩家客户端包
	 * ·-发送到某玩家区服所在的网关
	 * ·-然后强制转发到客户端
	 * </pre>
	 *
	 * @param code   协议号
	 * @param userId 玩家Id
	 * @param body   数据包
	 */
	public final static int sendOneClient(int code, int userId, Object body) {
		if (userId <= 0) {
			return 0;
		}
		Channel ch = getChannel(userId);
		if (ch == null) {
			return 0;
		}
		ByteBuf buf = MessageUtil.buildClientBuf(code, userId, body, ch);
		int size = buf.readableBytes();
		MessageUtil.writeChannel(buf, ch);
		Log.logConsole("--sendClient,userId:" + userId + ",code:" + code + ",body:" + JsonUtil.stringify(body));
		return size;
	}

	/**
	 * <pre>
	 * 发送服务器数据包 
	 * ·-发送到该玩家区服的网关
	 * ·-然后根据协议号进行转发到某类型进程(客户端除外)
	 * </pre>
	 *
	 * @param code   协议号
	 * @param userId 玩家Id
	 * @param body   数据包
	 * @return
	 */
	public final static int sendOneGate(int code, int userId, Object body) {
		if (userId <= 0) {
			return 0;
		}
		Channel ch = getChannel(userId);
		System.out.println();
		if (ch == null) {
			return 0;
		}
		Packet packet = new Packet();
		packet.setBody((byte[]) body);
		packet.setDesc(new PacketDesc(code, userId, false));
		ByteBuf buf = MessageUtil.buildBufByPacket(ch, packet);
		int size = buf.readableBytes();
		MessageUtil.writeChannel(buf, ch);
		return size;
	}

	/**
	 * <pre>
	 * 发送部分服务器数据包 
	 * ·-发送到多玩家区服的网关
	 * ·-然后根据协议号进行转发到某类型进程(客户端除外)
	 * </pre>
	 *
	 * @param code    协议号
	 * @param userIds 玩家Id集合
	 * @param body    数据包
	 */
	public final static void sendSomeGate(short code, Collection<Integer> userIds, Object body) {
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		List<Channel> chs = getChannel(userIds);
		if (chs == null || chs.isEmpty()) {
			return;
		}
		ByteBuf buf = MessageUtil.buildServerBuf(code, body, chs.get(0));
		MessageUtil.writeChannel(buf, chs, false);
	}

	/**
	 * <pre>
	 * 发送服务器数据包
	 * ·-发送到所有连接到本进程的网关
	 * ·-然后根据协议号进行转发到某类型进程(客户端除外)
	 * </pre>
	 *
	 * @param code
	 * @param body
	 */
	public final static void sendAllGate(int code, Object body) {
		List<Channel> chs = getAllGateChs();
		if (chs == null || chs.isEmpty()) {
			Log.error("网关连接列表为空...code:" + code + ",body:" + JsonUtil.stringify(body));
			return;
		}
		ByteBuf buf = MessageUtil.buildServerBuf(code, body, chs.get(0));
		MessageUtil.writeChannel(buf, chs, false);
	}

	/**
	 * <pre>
	 * 绑定玩家和区服Id
	 * </pre>
	 *
	 * @param userId
	 * @param serverId
	 */
	public final static void bindUserServer(int userId, int serverId) {
		user2Servers.put(userId, serverId);
		user2Channels.remove(userId);
	}

	/**
	 * <pre>
	 * 获取连接
	 * </pre>
	 *
	 * @param userId
	 * @return
	 */
	public final static Channel getChannel(int userId) {
		Channel ch = user2Channels.get(userId);
		if (ch != null) {
			return ch;
		}
		Integer serverId = user2Servers.get(userId);
		if (serverId != null) {
			List<Channel> chList = gateChannels.get(serverId);
			if (chList != null && !chList.isEmpty()) {
				ch = chList.get(RandomUtil.rand(chList.size()));
			}
		}
		if (ch != null) {
			user2Channels.put(userId, ch);
		}
		return ch;
	}

	/**
	 * <pre>
	 * 获取批量连接
	 * </pre>
	 *
	 * @param userIds
	 * @return
	 */
	private final static List<Channel> getChannel(Collection<Integer> userIds) {
		List<Channel> chList = new ArrayList<>();
		Set<Integer> serverIds = new HashSet<>();
		Channel channel = null;
		for (Integer userId : userIds) {
			channel = user2Channels.get(userId);
			if (channel != null) {
				Integer serverId = channel.attr(SocketServerHandler.GATE_KEY).get();
				if (serverId != null && !serverIds.contains(serverId)) {
					serverIds.add(serverId);
					chList.add(channel);
					continue;
				}
			}
			Integer serverId = user2Servers.get(userId);
			if (serverId != null && !serverIds.contains(serverId)) {
				List<Channel> tempList = gateChannels.get(serverId);
				if (tempList != null && !tempList.isEmpty()) {
					channel = tempList.get(RandomUtil.rand(tempList.size()));
					user2Channels.put(userId, channel);
					serverIds.add(serverId);
					chList.add(channel);
				}
			}
		}
		return chList;
	}

}
