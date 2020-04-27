/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 卸载
 * </pre>
 * 
 * @author reison
 * @time 2017年3月2日 上午11:18:04
 */
public interface Unloadable extends IModule {

	/** 卸载 */
	boolean unload();

}
