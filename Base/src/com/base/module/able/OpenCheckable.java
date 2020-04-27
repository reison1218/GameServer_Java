/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 需通过开关控制的
 * </pre>
 * 
 * @author reison
 */
public interface OpenCheckable extends IModule {

	/**
	 * <pre>
	 * 1、开关检测
	 * 2、等级检测
	 * ！等级功能开放用模板
	 * </pre>
	 *
	 * @return
	 */
	boolean isOpen();

}
