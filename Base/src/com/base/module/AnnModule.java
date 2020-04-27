/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <pre>
 * 模块单例类注解
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnModule {

	/** 模块名称 */
	String name();

	/** 模块数据表注释 */
	String comment();

	/**
	 * <pre>
	 * 与TableType 对应
	 * TableType=1 传统表，非Json格式，启服时不会自动建表，需要手动建表
	 * TableType=2 json格式（UserId+json）单玩家单条数据 继承JsonDatamodule
	 * TableType=3 json格式（id+UserId+json）单玩家多条数据
	 * TableType=4 Json格式（UserId+TemplateId+Json）单玩家多条数据，继承JsonTemDataModule
	 * </pre>
	 *
	 * @return Type
	 */
	TableType tableType();

	/** 是否需要初始化属性管理 */
	boolean needAttrModule() default false;

	/** 模块类型 ,详见ModuleType */
	short type();

	/**
	 * <pre>
	 * 绑定的排行榜类型：
	 * 1、排行榜模块对应的排行榜类型
	 * 2、养成模块绑定的排行榜类型
	 * </pre>
	 *
	 * @return
	 */
	String rankType() default "";

	/**
	 * <pre>
	 * 排行榜模块配置
	 * [0]发送数量
	 * [1]排序数量
	 * [2]上榜最小分数
	 * </pre>
	 *
	 * @return
	 */
	int[] rankConf() default { 0 };

	/**
	 * <pre>
	 * 养成模块配置
	 * [0]养成主类型
	 * [1]返回客户端协议号
	 * [2]道具移除类型
	 * [3]最大子类型
	 * [4]进度条型养成，点击一次增加经验
	 * </pre>
	 *
	 * @return
	 */
	int[] upConf() default { 0 };

	/**
	 * <pre>
	 * 固定值属性丹配置
	 * [0]属性丹道具模板Id
	 * [1]属性丹单个增加属性的模板Id
	 * [2]最大属性丹吞噬数量
	 * </pre>
	 *
	 * @return
	 */
	int[] attrItem() default { 0 };

	/**
	 * <pre>
	 * 千分比属性丹配置
	 * [0]属性丹道具模板Id
	 * [1]属性丹单个增加的千分比
	 * [2]最大属性丹吞噬数量
	 * </pre>
	 *
	 * @return
	 */
	int[] attrPerItem() default { 0 };

	/**
	 * <pre>
	 * 模块过期秒数
	 * ！主要用于共享模块淘汰冷数据
	 * </pre>
	 */
	int expireSecs() default Integer.MAX_VALUE;

}
