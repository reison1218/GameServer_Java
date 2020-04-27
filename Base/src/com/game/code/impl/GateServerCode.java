/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code.impl;

import com.game.code.AnnCodeDesc;
import com.game.code.ICode;

import io.netty.util.AttributeKey;

/**
 * <pre>
 * 网关接收协议号
 * 范围：5001-10000
 * ！对应GateLogic中的方法名
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@AnnCodeDesc(min = 5001, max = 10000)
public interface GateServerCode extends ICode {

	/** 非命令，玩家idkey **/
	public final static AttributeKey<Integer> USERID_KEY = AttributeKey.valueOf("userId");

	/** 连接验证返回 */
	int AUTH_BACK = 5001;

	/** 客户端心跳协议 */
	int HEART_BEAT = 5002;

	/** 玩家登陆返回 **/
	int LOGIN_IN = 5003;

	/** 购买星球 **/
	int BUY_HEATBALL = 5004;

	/** 合成星球 **/
	int MOVE_HEATBALL = 5005;

	/** 签到 **/
	int SIGN_IN = 5006;

	/** 转盘 **/
	int TURN_TABLE = 5007;

	/** 排行榜 **/
	int RANK = 5008;

	/** 资源 **/
	int RESOURCES = 5009;

	/** 战斗结算返回 **/
	int BATTLE_SETTLE = 5010;

	/** buff返回 **/
	int BUFF = 5011;

	/** 星球战斗位 **/
	int BATTLE_POS = 5012;

	/** 图鉴 **/
	int TUJIAN = 5013;

	/** 星球删除 **/
	int REMOVE = 5014;

	/** 看广告 **/
	int ADVERT = 5015;
	
	/** 转盘领取奖励 **/
	int TURN_TABLE_RESULT = 5016;
	
	/** 领取离线金币收益 **/
	int  RECEIVE_LINE_OFF_GOLD = 5017;
	
	/** 碎片合成 **/
	int  FRAGMENT_COMPOSE = 5018;
}
