/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.module.impl.cmd;

import com.base.module.AnnModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.module.cmd.BaseServerCmd;


/**
 * <pre>
 * 游戏服接收战斗服或跨服的指令处理  
 * ·- 关联玩法的公共命令
 * </pre>
 * 
 * @author reison
 */
@AnnModule(name = "", comment = "", tableType = TableType.NO_TABLE, type = ModuleType.CMD_MODULE)
public class GameServerCmd extends BaseServerCmd {

	public GameServerCmd() {
		super();
	}
}
