/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.module.impl.cmd;

import java.util.Collection;
import java.util.List;

import com.base.constant.ResConstants;
import com.base.key.PlayerKey;
import com.base.mgr.RankInfo;
import com.base.mgr.RankMgr;
import com.base.mgr.RankType;
import com.base.module.AnnModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.module.cmd.BaseServerCmd;
import com.base.netty.packet.Packet;
import com.base.redis.RedisKey;
import com.base.redis.RedisPool;
import com.base.template.AdvertInComeTemplate;
import com.base.template.HeatBallTemplate;
import com.base.template.TurnTableTemplate;
import com.base.template.base.BaseTemplate;
import com.base.template.mgr.AdvertInComeTemplateMgr;
import com.base.template.mgr.ConstantTemplateMgr;
import com.base.template.mgr.HeatBallTemplateMgr;
import com.base.template.mgr.TemplateMgr;
import com.base.template.mgr.TurnTableTemplateMgr;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.module.impl.HeatBallInventory;
import com.game.player.GamePlayer;
import com.google.protobuf.InvalidProtocolBufferException;
import com.utils.Log;
import com.utils.RandomUtil;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import proto.AdvertProto.AdvertReq;
import proto.AdvertProto.AdvertRsp;
import proto.BaseProto.RankInfoPt;
import proto.BaseProto.ResourcesPt;
import proto.BattleProto;
import proto.BattleProto.BattleSettleReq;
import proto.BattleProto.BattleSettleRsp;
import proto.RankProto.RankReq;
import proto.RankProto.RankRsp;
import proto.TurnTableProto;
import proto.TurnTableProto.TurnTableResultRsp;
import proto.UserProto.ReceiveLineOffGoldReq;
import proto.UserProto.ReceiveLineOffGoldRsp;

/**
 * <pre>
 * 游戏服接收战斗服或跨服 
 * ·-关联玩家的个人命令
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
@AnnModule(name = "", comment = "", tableType = TableType.NO_TABLE, type = ModuleType.CMD_MODULE)
public class GamePlayerCmd extends BaseServerCmd {

	public GamePlayerCmd() {
		super();
	}

	/**
	 * <pre>
	 * 游戏服接收战斗服或跨服 
	 * ·-关联玩家的命令
	 * ！通过op区分逻辑
	 * </pre>
	 * 
	 * @param reqInfo
	 */
	public final void handlerPlayerCmd(Packet packet) {
	}

	/**
	 * <pre>
	 * 网关通知玩家下线
	 * </pre>
	 *
	 * @param reqInfo
	 */
	public void logoff(Packet packet) {
		// 收到客户端断开网关连接
		Log.info("ClientGateWayLogOff,userId:" + packet.getUserId());
		// 执行下线逻辑
		GameMgr.logoffPlayer(packet.getUserId());
	}

	/**
	 * 玩家领取离线收益
	 * 
	 * @param packet
	 */
	public void receiveLineOffGold(Packet packet) {
		try {
			GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
			ReceiveLineOffGoldReq reqBuilder = ReceiveLineOffGoldReq.parseFrom(packet.getBody());
			ReceiveLineOffGoldRsp.Builder builder = ReceiveLineOffGoldRsp.newBuilder();
			// 校验广告id
			int advertId = reqBuilder.getAdvertId();
			if (advertId < 0) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.RECEIVE_LINE_OFF_GOLD, builder.build().toByteArray());
				return;
			}
			// 校验广告id
			AdvertInComeTemplateMgr mgr = (AdvertInComeTemplateMgr) TemplateMgr
					.getTemlateMgr(AdvertInComeTemplateMgr.class);
			AdvertInComeTemplate template = mgr.getTemplateById(advertId);
			if (template == null && advertId != 0) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.RECEIVE_LINE_OFF_GOLD, builder.build().toByteArray());
				return;
			}
			int multiple = 1;
			if (template != null) {
				multiple = template.getMultiple();
			}
			double gold = player.getDoubleData(PlayerKey.OFF_LINE_GOLD);
			// 判断离线金币数量
			if (gold <= 0) {
				builder.setIsSucc(false);
				builder.setErrMess("没有离线金币可以领取");
				player.sendClientPacket(GateServerCode.RECEIVE_LINE_OFF_GOLD, builder.build().toByteArray());
				return;
			}
			// 更新内存数据
			player.setData(PlayerKey.OFF_LINE_GOLD, 0);

			gold *= multiple;
			builder.setIsSucc(true);
			builder.setSysTime(TimeUtil.getCurSecs());
			ResourcesPt.Builder resBuilder = ResourcesPt.newBuilder();
			resBuilder.setId(ResConstants.GOLD);
			resBuilder.setNum(gold);
			builder.setResp(resBuilder);
			// 给玩家添加资源
			player.addRes(ResConstants.RES, ResConstants.GOLD, gold, true);
			player.sendClientPacket(GateServerCode.RECEIVE_LINE_OFF_GOLD, builder.build().toByteArray());
		} catch (Exception e) {
			Log.error("receiveLineOffGold has error!check it! message:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 获取排行榜
	 * 
	 * @param packet
	 */
	public void rank(Packet packet) {
		RankRsp.Builder builder = RankRsp.newBuilder();
		RankReq reqBuilder = null;
		try {
			reqBuilder = RankReq.parseFrom(packet.getBody());
			int userId = packet.getUserId();
			GamePlayer player = GameMgr.getCachePlayer(userId);
			int rankType = reqBuilder.getType();
			String rankKey = null;
			if (rankType == RankType.RANK_DAY) {
				rankKey = RedisKey.RANK_DAY_KEY;
			} else if (rankType == RankType.RANK_WEEK) {
				rankKey = RedisKey.RANK_WEEK_KEY;
			} else {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误!");
				player.sendClientPacket(GateServerCode.RANK, builder.build().toByteArray());
				return;
			}

			RankInfo myRankInfo = RankMgr.getPlayerRank(userId, rankKey);
			if (myRankInfo != null) {
				RankInfoPt.Builder riBuilder = RankInfoPt.newBuilder();
				riBuilder.setIndex(myRankInfo.getRank());
				riBuilder.setNickName(myRankInfo.getNickName());
				riBuilder.setAvatar(myRankInfo.getAvatar());
				riBuilder.setScore(myRankInfo.getScore());
				builder.setMyRank(riBuilder);
			}
			List<RankInfo> list = RankMgr.getAllRank(rankKey);
			if (list != null) {
				for (RankInfo ri : list) {
					RankInfoPt.Builder riBuilder = RankInfoPt.newBuilder();
					riBuilder.setIndex(ri.getRank());
					riBuilder.setNickName(ri.getNickName());
					riBuilder.setAvatar(ri.getAvatar());
					riBuilder.setScore(myRankInfo.getScore());
					builder.addRankPt(riBuilder);
				}
			}
			player.sendClientPacket(GateServerCode.RANK, builder.build().toByteArray());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 房间战斗结算（现在是单机，暂时把结算放在gameserver这边处理）
	 * 
	 * @param packet
	 */
	public void battleSettle(Packet packet) {
		BattleProto.BattleSettleRsp.Builder builder = BattleSettleRsp.newBuilder();
		int userId = packet.getUserId();
		GamePlayer player = GameMgr.getCachePlayer(userId);
		if (player == null) {
			Log.error("battleSettle player is null!userId:" + userId);
			return;
		}
		// 检查离线收益金币
		if (!player.checkOffLineGold()) {
			builder.setIsSucc(false);
			builder.setErrMess("非法操作");
			player.sendClientPacket(GateServerCode.BATTLE_SETTLE, builder.build().toByteArray());
			return;
		}

		// 校验展示位
		HeatBallInventory si = player.getModule(HeatBallInventory.class);
		if (si == null || si.getBattlePos() == 0) {
			builder.setIsSucc(false);
			builder.setErrMess("没有选择展示位");
			player.sendClientPacket(GateServerCode.BATTLE_SETTLE, builder.build().toByteArray());
			return;
		}

		// 正常逻辑
		try {
			BattleSettleReq reqBuilder = BattleSettleReq.parseFrom(packet.getBody());
			int type = reqBuilder.getType();
			int score = reqBuilder.getScore();
			int jump = reqBuilder.getJump();
			int jumpLevel = reqBuilder.getJumpLevel();
			int cp = reqBuilder.getCheckPoint();
			int cpMultiple = reqBuilder.getMultiple();

			// 校验参数
			if (type != RankType.RANK_DAY || type != RankType.RANK_WEEK) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误！");
				player.sendClientPacket(GateServerCode.BATTLE_SETTLE, builder.build().toByteArray());
				return;
			}
			// 校验参数
			if (score < 0 || jump < 0 || jumpLevel < 0 || cp < 0 || cpMultiple < 0) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误！");
				player.sendClientPacket(GateServerCode.BATTLE_SETTLE, builder.build().toByteArray());
				return;
			}

			// 玩家看广告额外收益
			int advertId = reqBuilder.getAdvertId();
			int multiple = 1;
			List<ResourcesPt.Builder> adList = null;
			AdvertInComeTemplateMgr adMgr = (AdvertInComeTemplateMgr) TemplateMgr
					.getTemlateMgr(AdvertInComeTemplateMgr.class);

			AdvertInComeTemplate adTemplate = adMgr.getTemplateById(advertId);
			if (adTemplate != null) {
				switch (adTemplate.getType()) {
				case AdvertInComeTemplate.RES:// 资源类型
					adList = adTemplate.getRewardList();
					break;

				case AdvertInComeTemplate.MULTIPLE:// 翻倍
					multiple = adTemplate.getMultiple();
					break;
				case AdvertInComeTemplate.REVIVE:// 复活
					break;
				}
			}

			HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);

			int slimeId = si.getTuJian();
			HeatBallTemplate st = mgr.getTemplateById(slimeId);

			double rewardGlod = st.getReward() * multiple;
			ConstantTemplateMgr cMgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);
			// 先校验玩家今日获得的次数
			int turnCount = player.getIntData(PlayerKey.TURN_COUNT);
			if (turnCount < cMgr.getIntValue("battle.turnTable.count")) {
				ResourcesPt.Builder tt = ResourcesPt.newBuilder();
				tt.setId(ResConstants.TURN_COUNT);
				tt.setNum(1);
				builder.addResp(tt);
			}

			// 更新玩家记录
			player.updateRecord(type,score, jump, jumpLevel, cp, cpMultiple);

			// 回给客户端
			ResourcesPt.Builder rb = ResourcesPt.newBuilder();
			rb.setId(ResConstants.GOLD);
			rb.setNum(rewardGlod);
			builder.addResp(rb);
			// 加上视频收益
			if (adList != null && !adList.isEmpty()) {
				for (ResourcesPt.Builder _builder : adList) {
					ResourcesPt.Builder _cBuilder = _builder;
					if (multiple > 1) {
						_cBuilder = _builder.clone();
					}
					_cBuilder.setNum(_builder.getNum() * multiple);
					builder.addResp(_cBuilder);
				}
			}
			builder.setIsSucc(true);
			player.sendClientPacket(GateServerCode.BATTLE_SETTLE, builder.build().toByteArray());
			// 给玩家加资源
			player.addRes(builder.getRespBuilderList(), true);
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 看广告收益
	 * 
	 * @param packet
	 */
	public void advert(Packet packet) {
		try {
			GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
			AdvertReq builder = AdvertReq.parseFrom(packet.getBody());
			AdvertRsp.Builder rspBuilder = AdvertRsp.newBuilder();
			int advertId = builder.getAdvertId();
			if (advertId <= 0) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误！");
				player.sendClientPacket(GateServerCode.ADVERT, rspBuilder.build().toByteArray());
				return;
			}
			AdvertInComeTemplateMgr mgr = (AdvertInComeTemplateMgr) TemplateMgr
					.getTemlateMgr(AdvertInComeTemplateMgr.class);
			AdvertInComeTemplate template = mgr.getTemplateById(advertId);
			if (template == null)
				return;
			List<ResourcesPt.Builder> list = null;
			switch (template.getType()) {
			case AdvertInComeTemplate.RES:
				list = template.getRewardList();
				break;
			}
			if (list == null)
				return;

			rspBuilder.setIsSucc(true);
			player.sendClientPacket(GateServerCode.ADVERT, rspBuilder.build().toByteArray());

			// 给玩家发资源
			player.addRes(list, true);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 转盘结果请求
	 * 
	 * @param packet
	 */
	public void turnTableResult(Packet packet) {

		try {
			TurnTableProto.TurnTableResultReq req = TurnTableProto.TurnTableResultReq.parseFrom(packet.getBody());
			TurnTableResultRsp.Builder builder = TurnTableResultRsp.newBuilder();
			int advertId = req.getAdvertId();

			GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
			if (advertId < 0) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.TURN_TABLE_RESULT, builder.build().toByteArray());
				return;
			}

			// 从redis里面把奖励拿出来
			String tt = RedisPool.hget(RedisKey.TURN_TABLE, Integer.toString(player.getUserId()));
			if (StringUtils.isEmpty(tt)) {
				builder.setIsSucc(false);
				builder.setErrMess("请先抽奖");
				player.sendClientPacket(GateServerCode.TURN_TABLE_RESULT, builder.build().toByteArray());
				return;
			}
			// 拿到奖励配置
			TurnTableTemplateMgr tMgr = (TurnTableTemplateMgr) TemplateMgr.getTemlateMgr(TurnTableTemplateMgr.class);
			TurnTableTemplate ttTemplate = tMgr.getById(Integer.parseInt(tt));

			AdvertInComeTemplateMgr mgr = (AdvertInComeTemplateMgr) TemplateMgr
					.getTemlateMgr(AdvertInComeTemplateMgr.class);
			AdvertInComeTemplate template = mgr.getTemplateById(advertId);

			int multiple = 1;
			if (template != null) {
				multiple = template.getMultiple();
			}

			// 返回客户端
			builder.setIsSucc(true);
			ResourcesPt.Builder _b = null;
			for (ResourcesPt.Builder b : ttTemplate.getRewardList()) {
				_b = b;
				if (multiple > 1) {
					_b = b.clone();
					_b.setNum(b.getNum() * multiple);
				}
				builder.addResp(_b);

			}
			player.sendClientPacket(GateServerCode.TURN_TABLE_RESULT, builder.build().toByteArray());

			// 给玩家加资源
			player.addRes(builder.getRespBuilderList(), true);
			// 删除redis奖励缓存
			RedisPool.hdel(RedisKey.TURN_TABLE, Integer.toString(player.getUserId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 转盘
	 * 
	 * @param packet
	 */
	public void turnTable(Packet packet) {
		TurnTableProto.TurnTableRsp.Builder builder = TurnTableProto.TurnTableRsp.newBuilder();
		GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
		int turnCount = player.getIntData(PlayerKey.TURN_COUNT);
//		ConstantTemplateMgr mgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);
//		int dayCount = Integer.parseInt((String) mgr.getValueById("battle.turnTable.count"));
		// 校验当前剩余次数
		if (turnCount <= 0) {
			builder.setIsSucc(false);
			builder.setErrMess("没有次数，无法抽奖");
			player.sendClientPacket(GateServerCode.TURN_TABLE, builder.build().toByteArray());
			return;
		}
		// 校验是否有未领取的奖励
		String tId = RedisPool.hget(RedisKey.TURN_TABLE, Integer.toString(player.getUserId()));
		if (!StringUtils.isEmpty(tId)) {
			builder.setIsSucc(false);
			builder.setErrMess("请先领取奖励再抽奖");
			player.sendClientPacket(GateServerCode.TURN_TABLE, builder.build().toByteArray());
			return;
		}
//		// 校验当前次数
//		if (turnCount >= dayCount) {
//			builder.setIsSucc(false);
//			builder.setErrMess("当天次数已经达上限，无法抽奖");
//			player.sendClientPacket(GateServerCode.TURN_TABLE, builder.build().toByteArray());
//			return;
//		}

		try {

			// 正常抽奖逻辑
			TurnTableTemplate tt = getTurnTableAward();
			if (tt == null) {
				Log.error("turnTable has error!check it!");
				return;
			}
			// 转盘次数-1
			player.addData(PlayerKey.TURN_COUNT, -1);
			// 返回客户端
			builder.setIsSucc(true);
			builder.setId((int) tt.getId());
			builder.setTurnCount(player.getIntData(PlayerKey.TURN_COUNT));
			player.sendClientPacket(GateServerCode.TURN_TABLE, builder.build().toByteArray());
			// 将玩家抽中的奖励放到redis缓存
			RedisPool.hset(RedisKey.TURN_TABLE, Integer.toString(player.getUserId()),
					Integer.toString((int) tt.getId()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 权重随机出配置
	 * 
	 * @return
	 */
	private TurnTableTemplate getTurnTableAward() {
		TurnTableTemplateMgr mgr = (TurnTableTemplateMgr) TemplateMgr.getTemlateMgr(TurnTableTemplateMgr.class);

		Collection<BaseTemplate> coll = mgr.getAll();

		// 计算总权重
		int total = mgr.getTotalWeight();
		// 进行权重计算
		int random = RandomUtil.rand(1, total + 1);
		int tmp = 0;
		for (BaseTemplate bt : coll) {
			TurnTableTemplate tt = (TurnTableTemplate) bt;
			tmp += tt.getProbability();
			if (tmp < random) {
				continue;
			}
			return tt;
		}
		return null;
	}
}
