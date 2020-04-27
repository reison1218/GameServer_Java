/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

/**
 * <pre>
 * 模块类型
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public interface ModuleType {

	/** 玩家一般模块(上线加载，下线卸载) */
	short PLAYER_MODULE = 1;

	/** 玩家常驻模块(起服加载，常驻内存，下线后冷数据会卸载) */
	short UNLOAD_MODULE = 2;

	/** 玩家排行榜模块(起服加载，常驻内存，下线不卸载) */
	short RANK_MODULE = 3;

	/** 服务器间通讯协议包处理模块-相当于Command(如跨服战场、跨服竞技等需要服务器间通讯的玩法) */
	short CMD_MODULE = 4;
	
	/** 充值档次数据记录模块(上线加载，下线卸载) */
	short CHARGE = 5;

}
