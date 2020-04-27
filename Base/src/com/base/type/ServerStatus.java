/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.type;

/**
 * <pre>
 * 当前服务器状态
 * </pre>
 * 
 * @author reison
 * @time 2018年3月9日 下午12:31:27
 */
public interface ServerStatus {

	/** 正常运行状态 */
	short RUNNING = 1;

	/** 0点正在重置状态 */
	short RESETTING = 2;

	/** 正在停服状态 */
	short STOPPING = 3;

}
