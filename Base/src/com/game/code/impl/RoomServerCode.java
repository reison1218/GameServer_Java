/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code.impl;

import com.game.code.AnnCodeDesc;
import com.game.code.ICode;

/**
 * <pre>
 * 房间协议
 * 范围：10001-15000
 * 由{@link RoomCmd}统一处理
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@AnnCodeDesc(min = 10001, max = 15000)
public interface RoomServerCode extends ICode {
	
	/**
	 * 登录绑定Uid->ServerId
	 */
	int LOG_IN = 10001;
	
	/**
	 * 登录绑定Uid->ServerId
	 */
	int LOG_OUT = 10002;

	/** 网关通知游戏服-玩家下线 */
	//@AnnReqCode(clazz = String.class, methodName = "logoff", needSendRes = false)
	//short LOG_OFF = 22001;

	


}
