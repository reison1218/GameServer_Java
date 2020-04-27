/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.netty.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.executor.ExecutorMgr;
import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketDesc;
import com.base.netty.packet.PacketKey;
import com.base.netty.util.MessageUtil;
import com.game.code.ICode;
import com.game.code.impl.GateServerCode;
import com.gate.bridge.BridgeMgr;
import com.gate.netty.action.ReConnectAction;
import com.gate.user.GateUserMgr;
import com.utils.JsonUtil;
import com.utils.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <pre>
 * Socket客户端
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public class SocketClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		handleDisconnect(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

	}

	/**
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Log.error("连接发生异常", cause);
		handleDisconnect(ctx);
	}

	/**
	 * <pre>
	 * 处理连接断开
	 * </pre>
	 *
	 * @param ctx
	 */
	protected void handleDisconnect(ChannelHandlerContext ctx) {
		// 异步处理断开连接重连
		ExecutorMgr.getDefaultExecutor().enDelayQueue(new ReConnectAction(5000));
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	/**
	 * @param ctx
	 * @throws Exception
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 验证并注册网关连接
		Map<String, Object> body = new HashMap<>();
		body.put(PacketKey.KEY, Short.MAX_VALUE);
		body.put(PacketKey.USERS_REG, GateUserMgr.getAllUserIds());
		int serverId = Config.getIntConfig(ConfigKey.SERVER_ID);
		body.put(ConfigKey.SERVER_ID, serverId);
		MessageUtil.writeChannel(MessageUtil.buildServerBuf(ICode.AUTH_GATE, body, ctx.channel()), ctx.channel());
	}

	/**
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		try {
			Packet packet = Packet.read(msg);
			PacketDesc desc = packet.getDesc();
			// 连接验证失败处理
			int code = desc.getCode();
			if (code == GateServerCode.AUTH_BACK) {
				JSONObject tempObj = (JSONObject) JsonUtil.parse(packet.getBody());
				if (tempObj == null || tempObj.getIntValue(PacketKey.RESULT) != 1) {
					ctx.channel().close();
					return;
				}
				return;
			}
			// 发送到客户端的包
			if (desc.isClient()) {
				if (code < BridgeMgr.getGateCodeRange()[0] || code > BridgeMgr.getGateCodeRange()[1]) {
					Log.error("发送到客户端的数据包code非法：" + packet);
					return;
				}
				GateUserMgr.sendPacket(packet);
			}
			// 服务端转发包
			else {
				BridgeMgr.arrangePacket(packet);
			}
		} catch (Throwable e) {
			Log.error("网关socket客户端接收包解析异常", e);
		}
	}

}
