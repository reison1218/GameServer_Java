/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.action;

import com.base.executor.Action;
import com.base.module.cmd.BaseServerCmd;
import com.base.netty.packet.Packet;
import com.game.mgr.GameMgr;

/**
 * <pre>
 * 处理服务器协议Action
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日 
 */
public final class CmdModuleHandleAction extends Action {

	private Packet packet;

	public CmdModuleHandleAction(BaseServerCmd module, Packet packet) {
		super(module);
		this.packet = packet;
	}

	
	@Override
	public void execute() {
		GameMgr.invokeCmdModule(packet);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "code:" + packet.getDesc().getCode() + packet.toString();
	}

}
