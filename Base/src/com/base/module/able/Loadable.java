/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 需加载的
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public interface Loadable extends IModule {

	/** 加载 */
	boolean load();

	/** 加载后逻辑 */
	void afterLoad();

}
