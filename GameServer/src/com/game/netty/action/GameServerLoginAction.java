package com.game.netty.action;

import java.util.Date;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.executor.Action;
import com.base.key.PlayerKey;
import com.base.mgr.GateChannelMgr;
import com.base.netty.LogInResult;
import com.game.mgr.GameMgr;
import com.game.netty.handler.GameSocketServerHandler;
import com.game.player.GamePlayer;
import com.utils.TimeUtil;

import proto.BaseProto.PlayerPt;
import proto.UserProto;

/**
 * 登陆action
 * 
 * @author reison
 *
 */
public class GameServerLoginAction extends Action {
	private GamePlayer player;
	UserProto.LoginReq req = null;
	private int serverId = 0;

	public GameServerLoginAction(GamePlayer player, UserProto.LoginReq req) {
		super(player);
		this.req = req;
		this.player = player;
		serverId = Config.getIntConfig(ConfigKey.SERVER_ID);
	}

	@Override
	public void execute() {
		// 从db去load
		player = GameMgr.loadPlayer(player);
		int userId = player.getUserId();
		if (player != null) {
			if (player.getDataMap().size() == 0) {
				// 新数据,只初始化必要数据
				player.setData(PlayerKey.AVATAR, req.getAvatar());
				player.setData(PlayerKey.NICK_NAME, req.getNickname());
				player.setNickNameWithSite(req.getNickname());
				player.setData(PlayerKey.CREATE_TIME, TimeUtil.getDateFormat(new Date()));
			}
			player.setData(PlayerKey.LAST_LOGIN_TIME, new Date());
			player.setData(PlayerKey.USER_OL, 1);
			// 绑定玩家区服->连接
			GateChannelMgr.bindUserServer(userId, serverId);
			// 通知网关登录成功
			player.send();
		} else {
			GameSocketServerHandler.sendGateRes(LogInResult.FAIL, player);
		}

	}

}
