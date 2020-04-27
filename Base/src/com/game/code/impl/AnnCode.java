/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 方法关联协议号
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AnnCode {

	/**
	 * <pre>
	 * 关联的协议号
	 * </pre>
	 *
	 * @return
	 */
	int code();

}
