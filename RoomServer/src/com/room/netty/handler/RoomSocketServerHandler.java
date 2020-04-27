package com.room.netty.handler;

import java.util.HashMap;
import java.util.Map;

import com.base.mgr.GateChannelMgr;
import com.base.netty.handler.SocketServerHandler;
import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketKey;
import com.game.code.impl.GameServerCode;
import com.utils.Log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * <pre>
 * 游戏服包处理Handler
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public class RoomSocketServerHandler extends SocketServerHandler {

	/**
	 * @param ctx
	 * @param jsonObj
	 */
	@Override
	protected void handleMsgReceived(ChannelHandlerContext ctx, Packet packet) {
		int code = packet.getDesc().getCode();
		int userId = packet.getDesc().getUserId();
		String s = new String(packet.getBody());
		Log.info("from gate, code:" + code + ",userid:" + userId + ",cmd:" + packet.getCode());
		// 组装通用对象
//		JSONObject jsonObj = (JSONObject) JsonUtil.parse(packet.getBody());
//		if (jsonObj == null) {
//			return;
//		}
//		ClientReqInfo reqInfo = new ClientReqInfo();
//		reqInfo.setOpType(jsonObj.getShortValue(PacketKey.OP));
//		reqInfo.setParam1(jsonObj.getIntValue(PacketKey.P1));
//		reqInfo.setParam2(jsonObj.getIntValue(PacketKey.P2));
//		reqInfo.setParam3(jsonObj.get(PacketKey.P3));
//		reqInfo.setParam4(jsonObj.get(PacketKey.P4));
//		reqInfo.setCode(code);
//		// 关联玩家的数据包(由玩家模块处理)
//		if (packet.getDesc().isClient()) {
//			int userId = packet.getDesc().getUserId();
//			if (code == GameServerCode.LOG_IN) {
//				logIn(userId, ctx.channel(), jsonObj);
//			} else {
//				other(userId, ctx.channel(), reqInfo);
//			}
//		}
//		// 玩法公共的数据包(由玩法模块处理)
//		else {
//			AnnReqCode annCode = (AnnReqCode) CodeMgr.getCodeAnn(code);
//			if (annCode == null && code != GameServerCode.LOG_IN) {
//				Log.error("找不到该协议号：" + code);
//				return;
//			}
//			if (annCode == null) {
//				return;
//			}
//			// 先获取队列(module是一个队列)
//			BaseServerCmd module = ModuleMgr.getCmdModule(annCode.clazz());
//			if (module == null) {
//				return;
//			}
//			// 异步处理包数据，防止阻塞netty-workLoop
//			ExecutorMgr.getServerExecutor().enqueue(module, new CmdModuleHandleAction(module, reqInfo));
//		}
//	}
//
//	private final void logIn(int userId, Channel channel, JSONObject jsonObj) {
//		if (userId < 1) {
//			Log.error("解析第一个数据包uid错误，请检查字段名" + userId);
//			RoomSocketServerHandler.sendGateRes(LogInResult.FAIL, userId);
//			return;
//		}
//		// key验证
//		String key = jsonObj.getString("key");
//		if (key == null) {
//			Log.error("解析第一个数据包key错误，请检查大小写或字段名" + jsonObj);
//			sendGateRes(LogInResult.FAIL, userId);
//			return;
//		}
	}

	/**
	 * <pre>
	 * 发送登录结果
	 * </pre>
	 *
	 * @param result
	 */
	public final static void sendGateRes(short result, int userId) {
		Map<String, Object> backLogin = new HashMap<>();
		backLogin.put("res", result);
		backLogin.put(PacketKey.USERID, userId);
		GateChannelMgr.sendAllGate(GameServerCode.LOG_IN, backLogin);
	}

	private final void other(int userId, Channel channel, Packet packet) {

	}

}
