package com.gate.action;

import com.base.executor.Action;
import com.base.executor.ActionQueue;
import com.game.code.impl.GateServerCode;
import com.gate.user.GateUserMgr;

import io.netty.channel.Channel;

public class DisConnectionAction extends Action {

	private Channel channel;

	public DisConnectionAction(ActionQueue queue, Channel channel) {
		super(queue);
		this.channel = channel;
	}

	@Override
	public void execute() {
		Integer userId = channel.attr(GateServerCode.USERID_KEY).get();
		GateUserMgr.logoffUser(userId, channel);
	}
}
