/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.action;

import java.util.List;

import com.base.executor.Action;
import com.base.executor.ActionQueue;
import com.base.mgr.GateChannelMgr;


/**
 * <pre>
 * 广播的action
 * </pre>
 * 
 * @author reison
 */
public class BroadAction extends Action {

	private List<Integer> broadUids; // 要发送的玩家Id

	private Object body; // 内容

	private short code;

	public BroadAction(ActionQueue queue, short code, List<Integer> broadUids, Object body) {
		super(queue);
		this.body = body;
		this.broadUids = broadUids;
		this.code = code;
	}

	@Override
	public void execute() {
		GateChannelMgr.broadClientPacket(code, body, broadUids);
	}
}
