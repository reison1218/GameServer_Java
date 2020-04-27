/**
 */
package com.gate.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketDesc;
import com.base.netty.util.MessageUtil;
import com.game.code.impl.GameServerCode;
import com.game.code.impl.RoomServerCode;
import com.gate.bridge.BridgeMgr;
import com.gate.bridge.CheckMgr;
import com.utils.Log;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * <pre>
 * 网关用户管理
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class GateUserMgr {

	/** 用户集合 */
	private final static Map<Integer, GateUser> users = new ConcurrentHashMap<>();

	/** 被踢掉的用户集合 */
	private final static Map<Integer, GateUser> kickUsers = new ConcurrentHashMap<>();

	/**
	 * <pre>
	 * 增加用户
	 * </pre>
	 *
	 * @param user
	 */
	public final static void addUser(GateUser user) {
		GateUser pre = users.remove(user.getUserId());
		if (pre != null) {
			kickUsers.put(user.getUserId(), pre);
		}
		users.put(user.getUserId(), user);
	}

	/**
	 * <pre>
	 * 获取用户
	 * </pre>
	 *
	 * @param user
	 */
	public final static GateUser getUser(int userId) {
		return users.get(userId);
	}

	/**
	 * <pre>
	 * 移除用户
	 * </pre>
	 *
	 * @param user
	 */
	public final static GateUser removeUser(int userId) {
		return users.remove(userId);
	}

	/**
	 * <pre>
	 * 用户下线
	 * </pre>
	 *
	 * @param user
	 */
	public final static GateUser logoffUser(int userId, Channel channel) {
		GateUser user = users.get(userId);
		if (user != null && channel == user.getChannel()) {
			Packet packet = new Packet();
			try {
				// 发给游戏服
				packet.setDesc(new PacketDesc(GameServerCode.DIS_CON, userId, false));
				BridgeMgr.arrangePacket(packet);

				// 发给房间服
				packet = new Packet();
				packet.setDesc(new PacketDesc(RoomServerCode.LOG_OUT, userId, false));
				BridgeMgr.arrangePacket(packet);
			} catch (Exception e) {
				Log.error("", e);
			}
			// 移除玩家
			removeUser(userId);
			CheckMgr.unload(userId);
		}
		// 移除会话
		channel.close();
		return user;
	}

	/**
	 * <pre>
	 * 获取所有用户
	 * </pre>
	 *
	 * @return
	 */
	public final static List<GateUser> getAllUsers() {
		return new ArrayList<>(users.values());
	}

	/**
	 * <pre>
	 * 发送或广播数据包到客户端
	 * </pre>
	 *
	 * @param packet
	 */
	public final static void sendPacket(Packet packet) {
		// 数据包单发
		if (!packet.getDesc().isBroad()) {
			int userId = packet.getDesc().getUserId();
			GateUser one = users.get(userId);
			if (one == null) {
				System.out.println("网关发包到客户端找不到玩家对象,userId：" + userId);
				return;
			}
			one.sendPacket(packet.buildFrame(one.getChannel()));
		}
		// 数据包需要广播
		else {
			Collection<Integer> uids = packet.getDesc().getBroadUids();
			List<GateUser> urs = new ArrayList<>();
			if (uids == null || uids.isEmpty()) {
				return;
			}
			for (int uid : uids) {
				GateUser user = users.get(uid);
				if (user != null && user.isOnline()) {
					urs.add(user);
				}
			}
			if (!urs.isEmpty()) {
				List<Channel> chs = new ArrayList<>(urs.size());
				BinaryWebSocketFrame buf = packet.buildFrame(urs.get(0).getChannel());
				for (GateUser user : urs) {
					chs.add(user.getChannel());
				}
				MessageUtil.writeChannel(buf, chs, false);
			}
		}
	}

	public static final Map<Integer, GateUser> getKickUsers() {
		return kickUsers;
	}

	/**
	 * <pre>
	 * 给所有玩家发送停服信息
	 * </pre>
	 */
	public static final void sendStopServer2All() {
		List<GateUser> allUsers = getAllUsers();
		for (GateUser user : allUsers) {
			user.sendStopServer();
		}
	}

	/**
	 * <pre>
	 * 获取网关所有玩家id
	 * </pre>
	 *
	 * @return
	 */
	public static final List<Integer> getAllUserIds() {
		return new ArrayList<>(users.keySet());
	}

}
