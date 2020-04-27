package com.gate.netty.handler;

import com.base.executor.ExecutorMgr;
import com.base.netty.handler.WebSocketServerHandler;
import com.base.netty.packet.Packet;
import com.game.code.impl.GameServerCode;
import com.game.code.impl.GateServerCode;
import com.gate.action.DisConnectionAction;
import com.gate.bridge.BridgeMgr;
import com.gate.user.GateUser;
import com.gate.user.GateUserMgr;
import com.utils.Log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import proto.UserProto;

/**
 * 路由websocket网络事件handler
 * 
 * @author reison
 *
 */
public class GateWebSocketServerHandler extends WebSocketServerHandler {

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		disConnection(ctx);
	}

	private void disConnection(ChannelHandlerContext ctx) {
		Integer userId = ctx.channel().attr(GateServerCode.USERID_KEY).get();
		if (userId == null)
			return;
		ExecutorMgr.getServerExecutor().enqueue(new DisConnectionAction(null, ctx.channel()));
	}

	@Override
	protected void handleMsgReceived(ChannelHandlerContext ctx, Packet packet) {

		int cmd = packet.getDesc().getCode();
		byte[] body = packet.getBody();
		try {
			if (cmd < 1) {
				Log.error("收到客户端数据包cmd非法：" + cmd);
				return;
			}
			Channel channel = ctx.channel();
			Integer userId = channel.attr(GateServerCode.USERID_KEY).get();
			// 第一个登录包(玩家绑定连接,加载数据)
			if (cmd == GameServerCode.LOG_IN) {
				proto.UserProto.LoginReq req = UserProto.LoginReq.parseFrom(body);
				if (req == null) {
					Log.error("LoginReq 解析失败！");
					return;
				}

				userId = req.getUserId();
				packet.getDesc().setUserId(userId);

				GateUser gateUser = GateUserMgr.getUser(userId);
				// 不等于null证明互挤
				if (gateUser != null) {
					if (gateUser.getChannel() != channel) {
						gateUser.getChannel().close();
						GateUserMgr.removeUser(userId);
					}
				}
				// 新增玩家会话数据
				GateUserMgr.addUser(new GateUser(userId, channel));
			}
			if (userId == null) {
				Log.error("玩家尚未登录,code:" + cmd + ", uid:" + userId);
				return;
			}
			// 设置消息包的userId
			packet.setUserId(userId);
			// 消息分发
			BridgeMgr.arrangePacket(packet);
		} catch (Throwable e) {
			Log.error("解析数据包JSON异常,code:" + cmd, e);
		}
	}
}
