/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 是否包含定时逻辑
 * </pre>
 * 
 * @author reison
 */
public interface Timerable extends IModule {

	/** 定时执行 */
	void timerExec();

}
