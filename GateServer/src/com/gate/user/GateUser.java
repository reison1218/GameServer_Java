/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.user;

import com.base.netty.util.MessageUtil;
import com.game.code.impl.GameServerCode;
import com.game.code.impl.GateServerCode;
import com.utils.TimeUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import proto.UserProto.HeartBeatRsp;

/**
 * <pre>
 * 网关用户
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class GateUser {

	/** 客户端连接 */
	private Channel channel;

	/** 用户Id */
	private int userId;

	public GateUser(int userId, Channel channel) {
		this.channel = channel;
		this.userId = userId;
	}

	public final Channel getChannel() {
		return channel;
	}

	public final void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * <pre>
	 * 玩家是否在线
	 * </pre>
	 *
	 * @return
	 */
	public final boolean isOnline() {
		return this.channel != null && this.channel.isActive();
	}

	public final int getUserId() {
		return userId;
	}

	/**
	 * <pre>
	 * 账号在别处登录
	 * </pre>
	 */
	public final void sendKicked() {
		sendDisconnMsg(GameServerCode.COMPRESS);
	}

	/**
	 * <pre>
	 * 账号被卸载数据
	 * ！客户端显示被踢下线
	 * </pre>
	 */
	public final void sendUnload() {
		sendDisconnMsg(GameServerCode.COMPRESS);
	}

	/**
	 * <pre>
	 * 账号被卸载数据
	 * ！客户端显示：维护中，请稍候
	 * </pre>
	 */
	public final void sendStopServer() {
		sendDisconnMsg(GameServerCode.COMPRESS);
	}

	private final void sendDisconnMsg(int code) {
		final Channel ch = this.channel;
		if (ch == null) {
			return;
		}
		if (!ch.isActive()) {
			ch.close();
			return;
		}
		ch.writeAndFlush(MessageUtil.buildEmpty(code, ch),
				ch.newPromise().addListener(new GenericFutureListener<Future<? super Void>>() {
					public void operationComplete(Future<? super Void> future) throws Exception {
						ch.close();
					}
				}));
	}

	/**
	 * <pre>
	 * 发送数据包到客户端
	 * </pre>
	 *
	 * @param frame
	 */
	public final void sendPacket(BinaryWebSocketFrame frame) {
		if (isOnline()) {
			MessageUtil.checkWritable(channel);
			this.channel.writeAndFlush(frame);
		}
		// 防止偶然的内存泄漏
		else {
			if (frame != null) {
				ReferenceCountUtil.release(frame);
			}
		}
	}

	/**
	 * <pre>
	 * 返回心跳
	 * </pre>
	 */
	public final void sendHeartBeat() {
		final Channel ch = this.channel;
		HeartBeatRsp.Builder builder = HeartBeatRsp.newBuilder();
		builder.setSysTime(TimeUtil.getCurSecs());
		sendPacket(MessageUtil.buildBinaryFrame(GateServerCode.HEART_BEAT, builder.build().toByteArray(), ch));
	}
}
