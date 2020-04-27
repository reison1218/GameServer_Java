/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.code.impl;

import com.game.code.AnnCodeDesc;
import com.game.code.ICode;
import com.game.module.impl.BuffInventory;
import com.game.module.impl.HeatBallInventory;
import com.game.module.impl.SignInInventory;
import com.game.module.impl.cmd.GamePlayerCmd;

/**
 * <pre>
 * 游戏服接收协议号
 * 范围：1001-5000
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
@AnnCodeDesc(min = 1001, max = 5000)
public interface GameServerCode extends ICode {

	int COMPRESS = 1001;

	/** 玩家登陆 **/
	int LOG_IN = 1002;

	/** 网关通知游戏服-玩家下线 */
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "logoff")
	int DIS_CON = 1003;

	/** 购买星球 */
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "buy", isPlayerModelCode = true)
	int BUY_HEATBALL = 1004;

	/** 移动星球 **/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "move", isPlayerModelCode = true)
	int MOVE_HEATBALL = 1005;

	/** 签到 **/
	@AnnReqCode(clazz = SignInInventory.class, methodName = "signIn",isPlayerModelCode = true)
	int SIGN_IN = 1006;

	/** 转盘 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "turnTable")
	int TURN_TABLE = 1007;

	/** 排行榜 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "rank")
	int RANK = 1008;

	/** 资源请求 **/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "calResources",isPlayerModelCode = true)
	int RESOURCES = 1009;

	/** 战斗结算 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "battleSettle")
	int BATTLE_SETTLE = 1010;

	/** buff **/
	@AnnReqCode(clazz = BuffInventory.class, methodName = "buff",isPlayerModelCode = true)
	int BUFF = 1011;

	/** 星球战斗位 **/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "battlePos",isPlayerModelCode = true)
	int BATTLE_POS = 1012;

	/** 星球图鉴 **/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "tuJian",isPlayerModelCode = true)
	int TUJIAN = 1013;

	/** 星球删除 **/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "remove",isPlayerModelCode = true)
	int REMOVE = 1014;

	/** 看广告 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "advert")
	int ADVERT = 1015;

	/** 转盘领取奖励 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "turnTableResult")
	int TURN_TABLE_RESULT = 1016;

	/** 领取离线金币收益 **/
	@AnnReqCode(clazz = GamePlayerCmd.class, methodName = "receiveLineOffGold")
	int RECEIVE_LINE_OFF_GOLD = 1017;
	
	/**碎片合成**/
	@AnnReqCode(clazz = HeatBallInventory.class, methodName = "move", isPlayerModelCode = true)
	int FRAGMENT_COMPOSE = 1018;
}
