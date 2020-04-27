/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty;

/**
 * <pre>
 * 登录结果
 * </pre>
 * 
 * @author reison
 * @time 2017年5月26日 下午12:13:09
 */
public interface LogInResult {

	/** 失败 */
	short FAIL = 0;

	/** 成功 */
	short SUCC = 1;

	/** 账号别处登录 */
	short KICK = 2;

	/** 账号被GM踢下线 */
	short UNLOAD = 3;

	/** 停服被踢下线 */
	short STOP = 4;

}
