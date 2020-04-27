/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.packet;

/**
 * <pre>
 * 数据包Key
 * </pre>
 * 
 * @author reison
 * @time 2017年5月17日 下午2:55:08
 */
public interface PacketKey {

	/** 是否需要广播 */
	String IS_BROAD = "_isb";

	/** 广播的玩家Id列表 */
	String BROAD_UIDS = "_bids";

	/** 接收者玩家Id */
	String USERID = "_uid";

	/** 数据包协议号 */
	String CODE = "_c";

	/** 实际发送的数据包 */
	String BODY = "_b";

	/** 是否为发送到客户端的包 */
	String IS_CLIENT = "_icl";

	/** 是否为发送到跨服的包 */
	String IS_CROSS = "_icr";

	/** 服务器之间的通讯Key */
	String KEY = "_k";

	/** 网关连接验证结果 */
	String RESULT = "_r";

	/** 网关进程序号Key */
	String GATE_IDX = "_idx";

	/** 通用数据包Key-op */
	String OP = "op";

	/** 通用数据包Key-p1 */
	String P1 = "p1";

	/** 通用数据包Key-p2 */
	String P2 = "p2";

	/** 通用数据包Key-p3 */
	String P3 = "p3";

	/** 通用数据包Key-p4 */
	String P4 = "p4";

	/** 通用数据包Key-p5 */
	String P5 = "p5";

	/** 战斗服、跨服重启后，网关注册玩家信息 */
	String USERS_REG = "users";

}
