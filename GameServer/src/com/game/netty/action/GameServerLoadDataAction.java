/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.netty.action;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.executor.Action;
import com.base.mgr.GateChannelMgr;
import com.base.netty.LogInResult;
import com.game.mgr.GameMgr;
import com.game.netty.handler.GameSocketServerHandler;
import com.game.player.GamePlayer;

import io.netty.channel.Channel;

/**
 * <pre>
 * 用户数据加载Action
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class GameServerLoadDataAction extends Action {

	private GamePlayer player;
	private Channel channel;
	private int serverId = 0;

	public GameServerLoadDataAction(GamePlayer player, Channel channel) {
		super(player);
		this.player = player;
		this.channel = channel;
		serverId = Config.getIntConfig(ConfigKey.SERVER_ID);
	}

	@Override
	public void execute() {
		// 从db去load
		player = GameMgr.loadPlayer(player);
		int userId = player.getUserId();
		if (player != null) {
			// 绑定玩家区服->连接
			GateChannelMgr.bindUserServer(userId, serverId);
			// 通知网关登录成功
			GameSocketServerHandler.sendGateRes(LogInResult.SUCC, player);
		} else {
			GameSocketServerHandler.sendGateRes(LogInResult.FAIL, player);
		}
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "player:" + player + ",channel:" + channel;
	}
}
