/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module.able;

import com.base.module.IModule;

/**
 * <pre>
 * 是否需发送客户端
 * </pre>
 * 
 * @author reison
 */
public interface Sendable extends IModule {

	/** 发送 */
	void send();

	/** 条件发送 */
	void sendSome(Object... params);

}
