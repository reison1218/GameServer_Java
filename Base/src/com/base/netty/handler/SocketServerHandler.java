/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;
import com.base.config.ConfigKey;
import com.base.mgr.GateChannelMgr;
import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketDesc;
import com.game.code.ICode;
import com.utils.JsonUtil;
import com.utils.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * <pre>
 * Socket处理Handler
 * </pre>
 * 
 * @author reison
 * @time 2017年4月17日
 */
public abstract class SocketServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private final static AtomicInteger counter = new AtomicInteger();

	public final static AttributeKey<Integer> GATE_KEY = AttributeKey.valueOf("ServerId");

	
	int COMPRESS = 1001;
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		handleDisconnect(ctx);
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
	protected final void handleDisconnect(ChannelHandlerContext ctx) {
		try {
			Integer serverId = ctx.channel().attr(SocketServerHandler.GATE_KEY).get();
			Log.error("网关连接断开：" + serverId);
			if (serverId == null) {
				return;
			}
			GateChannelMgr.removeChannel(serverId, ctx.channel());
		} catch (Exception e) {
			Log.error("处理连接断开异常", e);
		} finally {
			ctx.close();
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
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
		PacketDesc desc = null;
		int code = 0;
		try {
			Packet packet = Packet.read(msg);
			desc = packet.getDesc();
			code = desc.getCode();
			if (code == COMPRESS) {
				int temp = counter.incrementAndGet();
				if (temp % 10000 == 0) {
					System.out.println("已收到" + temp);
				}
			}
			// 验证网关连接
			if (code == ICode.AUTH_GATE) {
				JSONObject tempObj = (JSONObject) JsonUtil.parse(packet.getBody());
				if (tempObj == null) {
					return;
				}

				int serverId = tempObj.getIntValue(ConfigKey.SERVER_ID);
				if (serverId == 0) {
					Log.error("注册网关连接区服主键id错误,tempObj:" + tempObj);
					return;
				}
				GateChannelMgr.addChannel(tempObj, ctx.channel());
			} else {
				// 子类数据包逻辑
				handleMsgReceived(ctx, packet);
			}
		} catch (Throwable e) {
			Log.error("解析包异常，class:" + this.getClass().getSimpleName(), e);
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 处理收到的包
	 * </pre>
	 *
	 * @param ctx
	 * @param jsonObj
	 */
	protected abstract void handleMsgReceived(ChannelHandlerContext ctx, Packet packet);

}
