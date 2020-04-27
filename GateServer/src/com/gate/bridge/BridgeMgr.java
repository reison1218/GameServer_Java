/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.bridge;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.base.netty.packet.Packet;
import com.game.code.AnnCodeDesc;
import com.game.code.impl.GameServerCode;
import com.game.code.impl.GateServerCode;
import com.game.code.impl.RoomServerCode;
import com.gate.logic.GateLogic;
import com.utils.Log;

/**
 * <pre>
 * 转发管理
 * </pre>
 * 
 * @author reison
 */
public final class BridgeMgr {

	/** 网关接收协议号范围 */
	private final static short[] gateCodeRange = new short[2];

	/** 游戏服接收协议号范围 */
	private final static short[] gameCodeRange = new short[2];

	/** 房间接收协议号范围 */
	private final static short[] roomCodeRange = new short[2];

	/**
	 * <pre>
	 * 初始化协议号范围
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean init() {
		if (!initCodeRange(GateServerCode.class, gateCodeRange)) {
			return false;
		}
		if (!initCodeRange(GameServerCode.class, gameCodeRange)) {
			return false;
		}
		if (!initCodeRange(RoomServerCode.class, roomCodeRange)) {
			return false;
		}
		Log.info("网关转发协议初始化成功~");
		return true;
	}

	/**
	 * <pre>
	 * 初始化协议号范围
	 * 检测协议号合法性
	 * </pre>
	 *
	 * @param clazz
	 * @param range
	 * @return
	 */
	private final static boolean initCodeRange(Class<?> clazz, short[] range) {
		AnnCodeDesc codeDesc = clazz.getAnnotation(AnnCodeDesc.class);
		if (codeDesc == null) {
			Log.error("协议号文件缺失注解,clazz:" + clazz.getSimpleName());
			return false;
		}
		short min = codeDesc.min();
		short max = codeDesc.max();
		Set<Short> codeSet = new HashSet<>();
		Field[] fs = clazz.getDeclaredFields();
		for (int i = 0, len = fs.length; i < len; i++) {
			Field f = fs[i];
			try {
				short code = 0;
				try {
					code = f.getShort(null);
				} catch (Exception e) {
					// Log.error("协议号获取异常,clazz:" + clazz.getSimpleName() + ",field:" +
					// f.getName());
					// return false;
					continue;
				}
				if (code > max) {
					Log.error("协议号超过本类型上限,clazz:" + clazz.getSimpleName() + ",field:" + f.getName() + ",code:" + code
							+ ",max:" + max);
					return false;
				}
				if (code < min) {
					Log.error("协议号超过本类型下限,clazz:" + clazz.getSimpleName() + ",field:" + f.getName() + ",code:" + code
							+ ",min:" + min);
					return false;
				}
				if (codeSet.contains(code)) {
					Log.error("协议号重复,clazz:" + clazz.getSimpleName() + ",field:" + f.getName() + ",code:" + code);
					return false;
				}
				codeSet.add(code);
			} catch (Exception e) {
				Log.error("初始化协议号范围异常", e);
				return false;
			}
		}
		range[0] = min;
		range[1] = max;
		return true;
	}

	/**
	 * <pre>
	 * 分发数据包
	 * </pre>
	 *
	 * @param packet
	 */
	public final static void arrangePacket(Packet packet) {
		int code = packet.getDesc().getCode();
		if (!CheckMgr.checkPkgRate(packet.getDesc().getUserId(), code)) {
			return;
		}
		// 游戏服务器数据包
		if ((code >= gameCodeRange[0] && code <= gameCodeRange[1])) {
			ChannelMgr.send2Game(packet);
		}

		// 房间服务器数据包
		else if ((code >= roomCodeRange[0] && code <= roomCodeRange[1])) {
			ChannelMgr.send2Room(packet);
		}

		// 网关自处理数据包
		else if (code >= gateCodeRange[0] && code <= gateCodeRange[1]) {
			GateLogic.handle(packet);
		}
		// 未知的包,丢弃
		else {
			Log.info("网关收到未知的数据包:" + packet);
		}
	}

	public final static short[] getServerCodeRange() {
		return gameCodeRange;
	}
	
	public final static short[] getGateCodeRange() {
		return gateCodeRange;
	}

	public final static void main(String[] args) {
		init();
	}

}
