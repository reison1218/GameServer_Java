/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.netty.handler;

import java.util.Date;

import com.base.executor.ExecutorMgr;
import com.base.key.PlayerKey;
import com.base.mgr.GateChannelMgr;
import com.base.module.ModuleMgr;
import com.base.module.cmd.BaseServerCmd;
import com.base.netty.LogInResult;
import com.base.netty.action.CmdModuleHandleAction;
import com.base.netty.handler.SocketServerHandler;
import com.base.netty.packet.Packet;
import com.game.code.CodeMgr;
import com.game.code.impl.AnnReqCode;
import com.game.code.impl.GameServerCode;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.netty.action.GameServerCodeHandleAction;
import com.game.netty.action.GameServerLoadDataAction;
import com.game.netty.action.GameServerLoginAction;
import com.game.player.GamePlayer;
import com.google.protobuf.InvalidProtocolBufferException;
import com.utils.Log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import proto.UserProto;

/**
 * <pre>
 * 游戏服包处理Handler
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public class GameSocketServerHandler extends SocketServerHandler {

	/**
	 * @param ctx
	 * @param packet
	 */
	@Override
	protected void handleMsgReceived(ChannelHandlerContext ctx, Packet packet) {

		int code = packet.getDesc().getCode();
		int userId = packet.getDesc().getUserId();
		Log.info("packet from gate,userid:" + userId + ",cmd:" + packet.getCode());

		// 先处理没有注解的命令号
		// 判断是否为登录
		if (code == GameServerCode.LOG_IN) {
			logIn(userId, ctx.channel(), packet);
			return;
		} else if (code == GameServerCode.COMPRESS) {// 判断是否为压测
			ByteBuf byteBuf = ctx.channel().alloc().buffer();
			if (byteBuf == null) {
				byteBuf = Unpooled.buffer();
			}
			byteBuf.writeInt(16);
			byteBuf.writeInt(GameServerCode.COMPRESS);
			byteBuf.writeInt(0);
			byteBuf.writeInt(0);
			byteBuf.writeInt(12);
			ctx.channel().writeAndFlush(byteBuf);
			return;
		} else {
			GamePlayer player = GameMgr.getCachePlayer(userId);
			// 热加载机制
			if (player == null) {
				// 是否正在加载中
				if (GameMgr.checkInitLoading(userId)) {
					return;
				}
				Log.error("收到协议找不到玩家,执行热加载,userId:" + userId + packet);
				// 先新建GamePlayer(保证某个玩家登录使用自己的队列异步加载数据，不阻塞其余数据包)
				player = new GamePlayer(userId);
				ExecutorMgr.getPlayerExecutor().enqueue(player, new GameServerLoadDataAction(player, ctx.channel()));
				return;
			}

			// 拿到命令对应的注解
			AnnReqCode annCode = CodeMgr.getCodeAnn(code);
			// 关联玩家的数据包(由玩家模块处理)
			if (annCode.isPlayerModelCode()) {
				playerModelHandler(userId, ctx.channel(), packet);
			} else {// 其他的由单例模块处理
				// 先获取队列(module是一个队列)
				BaseServerCmd module = ModuleMgr.getCmdModule(annCode.clazz());
				if (module == null) {
					Log.error("找不到协议号对应的模块：" + code);
					return;
				}
				// 异步处理包数据，防止阻塞netty-workLoop
				ExecutorMgr.getServerExecutor().enqueue(module, new CmdModuleHandleAction(module, packet));
			}
		}

	}

	/**
	 * 登陆函数
	 * 
	 * @param channel
	 * @param packet
	 */
	private final void logIn(int userId, Channel channel, Packet packet) {
		if (userId < 1) {
			Log.error("解析第一个数据包uid错误，请检查字段名" + userId);
			return;
		}

		// 获取内存玩家
		GamePlayer player = GameMgr.getCachePlayer(userId);
		// 如果内存没有，则从db去load
		if (player == null) {
			try {
				// 是否正在加载中
				if (GameMgr.checkInitLoading(userId)) {
					return;
				}
				UserProto.LoginReq builder = UserProto.LoginReq.parseFrom(packet.getBody());
				player = new GamePlayer(userId);
				ExecutorMgr.getPlayerExecutor().enqueue(new GameServerLoginAction(player, builder));
			} catch (InvalidProtocolBufferException e) {
				Log.error(e.getMessage());
			}
			Log.info("收到登陆协议包,cmd:" + GameServerCode.LOG_IN + ",执行玩家数据加载,userId:" + userId);
		} else {// 如果已经有数据代表已经登录了

			// 更新登陆时间
			player.setData(PlayerKey.LAST_LOGIN_TIME, new Date());
			// 更新在线状态
			player.setData(PlayerKey.USER_OL, 1);
			// 通知网关登录成功
			player.send();
		}
	}

	/**
	 * <pre>
	 * 发送登录结果
	 * </pre>
	 *
	 * @param result
	 */
	public final static void sendGateRes(short result, GamePlayer player) {
		// 登陆成功，返回客户端消息
		if (result != LogInResult.SUCC) {
			return;
		}
		UserProto.LoginRsp.Builder builder = UserProto.LoginRsp.newBuilder();
		builder.setIsSucc(true);
		builder.setUserId(player.getUserId());
		builder.setLastLoginTime(player.getLastLoginTimeSecs());
		builder.setAvatar(player.getStringData(PlayerKey.AVATAR));
		GateChannelMgr.sendOneGate(GateServerCode.LOGIN_IN, player.getUserId(), builder.build().toByteArray());
	}

	/**
	 * 处理非登录的命令请求，在此做命令转发
	 * 
	 * @param userId
	 * @param channel
	 * @param reqInfo
	 */
	private final void playerModelHandler(int userId, Channel channel, Packet packet) {
		// 后续数据包
		if (userId < 1) {
			Log.error("解析数据包uid错误，请检查字段名" + packet);
			return;
		}
		GamePlayer player = GameMgr.getCachePlayer(userId);
		if (player == null) {
			Log.error("player is null for userId:" + userId);
			return;
		}
		// 异步处理包数据，防止阻塞netty-workLoop
		ExecutorMgr.getPlayerExecutor().enqueue(player, new GameServerCodeHandleAction(player, packet));
	}

}
