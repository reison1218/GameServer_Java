/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.logic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.base.executor.CommonExecutor;
import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketKey;
import com.game.code.impl.AnnCode;
import com.game.code.impl.GateServerCode;
import com.game.code.impl.RoomServerCode;
import com.gate.bridge.ChannelMgr;
import com.gate.user.GateUser;
import com.gate.user.GateUserMgr;
import com.utils.Log;

/**
 * <pre>
 * 网关逻辑处理
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class GateLogic {

	/** 网关执行器 */
	private final static CommonExecutor defaultExecutor = new CommonExecutor(2, "GateDefault");

	/** 逻辑方法缓存 */
	private final static Map<Integer, Method> methods = new ConcurrentHashMap<>();

	/**
	 * <pre>
	 * 获取执行器
	 * </pre>
	 *
	 * @return
	 */
	public static final CommonExecutor getDefaultexecutor() {
		return defaultExecutor;
	}

	/**
	 * <pre>
	 * 初始化方法
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean init() {
		Method[] ms = GateLogic.class.getDeclaredMethods();
		for (int i = 0, len = ms.length; i < len; i++) {
			Method m = ms[i];
			AnnCode annCode = m.getDeclaredAnnotation(AnnCode.class);
			if (annCode != null && Modifier.isStatic(m.getModifiers())) {
				Method preM = methods.get(annCode.code());
				String name = m.getName();
				if (preM != null) {
					Log.error("GateLogic中存在协议号重复,pre：" + preM.getName() + ",new:" + name);
					return false;
				}
				methods.put(annCode.code(), m);
			}
		}
		Log.error("网关逻辑处理方法初始化成功,count：" + methods.size());
		return true;
	}

	/**
	 * <pre>
	 * 网关逻辑处理
	 * </pre>
	 *
	 * @param packet
	 */
	public final static void handle(Packet packet) {
		Method m = methods.get(packet.getDesc().getCode());
		if (m == null) {
			Log.error("找不到协议号关联的方法,code:" + packet.getDesc().getCode());
			return;
		}
		try {
			m.invoke(null, packet);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.error("反射调用网关处理逻辑异常", e);
		}
	}

	// =========以下为网关逻辑的函数==============================================================================================

	/**
	 * <pre>
	 * 游戏服登录验证返回
	 * </pre>
	 * 
	 * @param packet
	 * @return
	 */
	@AnnCode(code = GateServerCode.LOGIN_IN)
	private final static boolean logIn(Packet packet) {
		GateUser user = GateUserMgr.getUser(packet.getUserId());
		if (user == null) {
			return false;
		}
		user.getChannel().attr(GateServerCode.USERID_KEY).set(packet.getUserId());
		// 返回客户端
		loginRes(user, packet);
		GateUserMgr.getKickUsers().remove(packet.getUserId());
		return true;
	}

	/**
	 * 登陆返回
	 * 
	 * @param user
	 */
	private static void loginRes(GateUser user, Packet packet) {
		user.sendPacket(packet.buildFrame(user.getChannel()));
	}

	/**
	 * <pre>
	 * 注册房间服
	 * </pre>
	 *
	 * @param userId
	 */
	private final static void loginServers(int userId) {
		Map<String, Object> body = new HashMap<>();
		body.put(PacketKey.P1, userId);
		ChannelMgr.send2Room(RoomServerCode.LOG_IN, body);
	}

	/**
	 * <pre>
	 * 离开战斗服、跨服
	 * </pre>
	 *
	 * @param userId
	 */
	private final static void logoffServers(int userId) {
		Map<String, Object> body = new HashMap<>();
		body.put(PacketKey.P1, userId);
		// ChannelMgr.send2Room(RoomCode.LOG_OFF, body);
	}

	/**
	 * <pre>
	 * 客户端心跳
	 * </pre>
	 *
	 * @param packet
	 * @return
	 */
	@AnnCode(code = GateServerCode.HEART_BEAT)
	private final static boolean heartBeat(Packet packet) {
		int userId = packet.getDesc().getUserId();
		GateUser user = GateUserMgr.getUser(userId);
		if (user == null) {
			return false;
		}
		// if (!CheckMgr.checkHeartBeat(userId, true)) {
		// Log.error("收到心跳包发现玩家已掉线，断开玩家连接,userId：" + userId);
		// user.forceDisconnect();
		// return true;
		// }
		user.sendHeartBeat();
		return true;
	}

}
