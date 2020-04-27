/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.netty.action;

import com.base.executor.Action;
import com.base.netty.packet.Packet;
import com.game.mgr.GameMgr;
import com.game.player.GamePlayer;

/**
 * <pre>
 * 处理客户端协议Action
 * </pre>
 * 
 * @author reison
 */
public final class GameServerCodeHandleAction extends Action {

	private Packet packet;
	private GamePlayer player;

	public GameServerCodeHandleAction(GamePlayer player, Packet packet) {
		super(player);
		this.packet = packet;
		this.player = player;
	}

	@Override
	public void execute() {
		GameMgr.invoke(player, packet);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "code:" + packet.getCode() + packet.toString() + ",player:" + player;
	}
}
