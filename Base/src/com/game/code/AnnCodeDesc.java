/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 协议号类描述
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnCodeDesc {

	/**
	 * <pre>
	 * 协议号最小值
	 * </pre>
	 *
	 * @return
	 */
	short min();

	/**
	 * <pre>
	 * 协议号最大值
	 * </pre>
	 *
	 * @return
	 */
	short max();

}
