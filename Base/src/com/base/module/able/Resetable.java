/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 模块重置接口
 * </pre>
 * 
 * @author reison
 */
public interface Resetable extends IModule {

	/** 每日0点重置 */
	boolean dayReset();

}
