/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.key;

/**
 * <pre>
 * 玩家key
 * </pre>
 * 
 * @author reison
 */
public interface PlayerKey {

	/** 玩家id **/
	public final static String USER_ID = "userId";
	/** 是否在线 **/
	public final static String USER_OL = "ol";

	/** 头像 **/
	public final static String AVATAR = "avatar";

	/** 昵称 **/
	public final static String NICK_NAME = "nickName";

	/** 金币 **/
	public final static String GOLD = "gold";
	/** 钻石 **/
	public final static String DIAMONDS = "diamonds";
	/** 转盘次数 **/
	public final static String TURN_COUNT = "turnCount";
	/** 最高得分 **/
	public final static String MAX_SCORE = "maxScore";
	/** 最高跳跃范围 **/
	public final static String MAX_JUMP_RANGE = "maxJumpRange";
	/** 最大高倍数 **/
	public final static String MAX_MULTIPLE = "maxMultiple";
	/** 最大跳跃段数 **/
	public final static String MAX_JUMP_LEVEL = "maxJumpLevel";
	/** 最大关卡 **/
	public final static String MAX_CP = "maxcp";

	/** 创建时间 **/
	public final static String CREATE_TIME = "ctime";

	/** 修改时间 **/
	public final static String UPDATE_TIME = "mtime";

	/** 最近一次登录时间 **/
	public final static String LAST_LOGIN_TIME = "lastLoginTime";

	/** 最近一次登录ip **/
	public final static String LAST_LOGIN_IP = "lastLoginIp";

	/** 累积在线时间(无限累加,单位秒) */
	public final static String TOTLA_ONLINE_TIME = "totime";

	/** 记录下线时间 */
	public final static String OFF_TIME = "offlinetime";

	/** 离线收益金币 */
	public final static String OFF_LINE_GOLD = "offlineGold";

	/** 玩家模块重置时间 */
	public final static String RESET_TIME = "rtime";
}
