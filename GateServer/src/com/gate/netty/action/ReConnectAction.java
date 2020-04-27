/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.gate.netty.action;

import com.base.executor.DelayAction;
import com.base.executor.ExecutorMgr;
import com.gate.bridge.ChannelMgr;

/**
 * <pre>
 * 游戏服重连Action
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public final class ReConnectAction extends DelayAction {

	public ReConnectAction(long delay) {
		super(ExecutorMgr.getDefaultExecutor().getActionQueue(), delay);
	}

	/**
	 */
	@Override
	public void execute() {
		if (!ChannelMgr.reconnRoom()) {
			enDelayQueue();
		}

		if (!ChannelMgr.reconnGame()) {
			enDelayQueue();
		}
	}

}
