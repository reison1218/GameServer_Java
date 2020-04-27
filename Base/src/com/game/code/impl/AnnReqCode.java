/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.base.module.IModule;


/**
 * <pre>
 * 客户端请求协议号注解
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnnReqCode {

	/**
	 * <pre>
	 * 关联模块class
	 * </pre>
	 *
	 * @return
	 */
	Class<? extends IModule> clazz();

	/**
	 * <pre>
	 * 关联模块方法名
	 * </pre>
	 *
	 * @return
	 */
	String methodName();

	/**
	 * <pre>
	 * 是否需要立马返回成功失败
	 * </pre>
	 *
	 * @return
	 */
	boolean needQuickRes() default false;

	/**
	 * <pre>
	 * 是否需要统一更新数据到客户端
	 * </pre>
	 *
	 * @return
	 */
	boolean needSendRes() default true;

	/**
	 * <pre>
	 * 是否需要打印接收协议的日志
	 * </pre>
	 *
	 * @return
	 */
	boolean needPrintLog() default true;

	/**
	 * <pre>
	 * 是否刷新红点
	 * </pre>
	 *
	 * @return
	 */
	boolean refreshRedDot() default false;

	/**
	 * <pre>
	 * 模块名称,用于开关检查，填写数据来自t_s_permission，不填写默认与开关不关联。
	 * </pre>
	 * 
	 * @return
	 */
	int belongModelId() default 0;

	/**
	 * <pre>
	 * 是否为客户端发起的协议号
	 * </pre>
	 *
	 * @return
	 */
	boolean isClientCode() default false;
	
	/**
	 * <pre>
	 *  是否是玩家模块
	 * </pre>
	 *
	 * @return
	 */
	boolean isPlayerModelCode() default false;

}
