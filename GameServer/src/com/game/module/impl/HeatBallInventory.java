package com.game.module.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.base.constant.ResConstants;
import com.base.key.HeatBallKey;
import com.base.key.PlayerKey;
import com.base.module.AnnModule;
import com.base.module.JsonDataModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.module.able.Resetable;
import com.base.netty.packet.Packet;
import com.base.template.HeatBallTemplate;
import com.base.template.base.BaseTemplate;
import com.base.template.mgr.ConstantTemplateMgr;
import com.base.template.mgr.HeatBallTemplateMgr;
import com.base.template.mgr.TemplateMgr;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.player.GamePlayer;
import com.google.protobuf.InvalidProtocolBufferException;
import com.utils.Log;
import com.utils.TimeUtil;

import proto.BaseProto;
import proto.BaseProto.HeatBallPt;
import proto.BaseProto.ResourcesPt;
import proto.HeatBallProto;
import proto.HeatBallProto.BattlePosReq;
import proto.HeatBallProto.BattlePosRsp;
import proto.HeatBallProto.FragmentComposeReq;
import proto.HeatBallProto.FragmentComposeRsp;
import proto.HeatBallProto.MoveHeatBallReq;
import proto.HeatBallProto.RemoveReq;
import proto.HeatBallProto.RemoveRsp;
import proto.HeatBallProto.TuJianRsp;
import proto.UserProto.ResoucesRsp;

/**
 * 玩家星球模块
 * 
 * @author reison
 *
 */
@AnnModule(name = "heatBall", comment = "星球", tableType = TableType.JSON_TABLE_ONLY, type = ModuleType.PLAYER_MODULE)
public class HeatBallInventory extends JsonDataModule implements Resetable {

	public HeatBallInventory(GamePlayer player) {
		super(player);
	}

	/** 基数 **/
	private double base = 0;
	/** 乘数 **/
	private double multiplier = 0;

	/** 减数1 **/
	private double subtrahend1 = 0;
	/** 加数 **/
	private double addend = 0;

	/** 减数2 **/
	private double subtrahend2 = 0;
	/** 除数 **/
	private double divisor = 0;
	/** 离线收益时长 **/
	private int logOffIncome = 0;
	/** 收益时间（单位s） **/
	private int INCOME_TIME = 0;

	/** 金币购买需要减去的等级 **/
	private int goldBuy = 0;

	/** 钻石购买需要减去的等级 **/
	private int diamondBuy = 0;

	class HeatBallBuyTimes {
		int id;
		int buyTime;

		public HeatBallBuyTimes(int _id, int _buyTime) {
			this.id = _id;
			this.buyTime = _buyTime;
		}
	}

	@Override
	public void initData() {
		// 初始化公式需要的配置
		ConstantTemplateMgr mgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);
		// 先获得基数
		String str = (String) mgr.getValueById("buy.base");
		base = Double.parseDouble(str);
		// 获得乘数
		str = (String) mgr.getValueById("buy.multiplier");
		multiplier = Double.parseDouble(str);
		// 再获得减数
		str = (String) mgr.getValueById("buy.subtrahend1");
		subtrahend1 = Double.parseDouble(str);
		str = (String) mgr.getValueById("buy.subtrahend2");
		subtrahend2 = Double.parseDouble(str);
		// 获得加数
		str = (String) mgr.getValueById("buy.addend");
		addend = Double.parseDouble(str);
		// 获得除数
		str = (String) mgr.getValueById("buy.divisor");
		divisor = Double.parseDouble(str);
		str = (String) mgr.getValueById("off.income");
		logOffIncome = Integer.parseInt(str);
		str = (String) mgr.getValueById("income.time");
		INCOME_TIME = Integer.parseInt(str);

		// 获取购买需要的减去等级配置
		str = (String) mgr.getValueById("gold.buy");
		goldBuy = Integer.parseInt(str);

		str = (String) mgr.getValueById("diamond.buy");
		diamondBuy = Integer.parseInt(str);

		// 初始化图鉴
		getTuJian();

		// 初始化星球数组
		getHeatBallData();
	}

	/**
	 * 获得星球数据
	 * 
	 * @return
	 */
	public JSONArray getHeatBallData() {
		JSONArray array = getData(HeatBallKey.HEATBALL);
		if (array == null) {
			array = new JSONArray();
			for (int i = 0; i < HeatBallKey.HEATBALL_MAX_SIZE; i++) {
				array.add(0);
				// 默认给一个1级的星球
				if (i == 0) {
					array.set(i, 1);
				}
			}
			setData(HeatBallKey.HEATBALL, array);
		}
		return array;
	}

	/**
	 * 获取购买次数
	 * 
	 * @param headBallId
	 * @return
	 */
	public int getBuyTimes(int headBallId) {
		Map<Integer, Integer> map = getData(HeatBallKey.BUY_TIMES);
		if (map == null)
			return 0;
		Integer buyTimes = map.get(headBallId);
		if (buyTimes == null) {
			return 0;
		}
		return buyTimes.intValue();
	}

	/**
	 * 设置购买次数
	 * 
	 * @param headBallId
	 * @param buyTimes
	 */
	public void setBuyTimes(int headBallId, int buyTimes) {
		Map<Integer, Integer> map = getData(HeatBallKey.BUY_TIMES);
		if (map == null) {
			map = new HashMap<Integer, Integer>();
			setData(HeatBallKey.BUY_TIMES, map);
		}
		map.put(headBallId, buyTimes);
	}

	/**
	 * 添加购买次数
	 * 
	 * @param headBallId
	 * @return
	 */
	public int addBuyTimes(int headBallId) {
		Map<Integer, Integer> map = getData(HeatBallKey.BUY_TIMES);
		if (map == null) {
			map = new HashMap<Integer, Integer>();
			setData(HeatBallKey.BUY_TIMES, map);
		}
		Integer buyTimes = map.get(headBallId);
		if (buyTimes == null) {
			buyTimes = 0;
		}
		buyTimes++;
		map.put(headBallId, buyTimes);
		return buyTimes.intValue();
	}

	@Override
	public void afterLoad() {
		super.afterLoad();
		// 计算离线收益
		GamePlayer player = GameMgr.getCachePlayer(getUserId());
		this.calcLionOffIncome(player);
	}

	@Override
	public boolean dayReset() {
		return false;
	}

	/**
	 * 计算玩家资源（金币）
	 * 
	 * @param packet
	 */
	public void calResources(Packet packet) {
		GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
		// 计算星球收益
		double glod = calcIncome(player);
		// 返回客户端
		ResoucesRsp.Builder builder = ResoucesRsp.newBuilder();
		builder.setSysTime(TimeUtil.getCurSecs());
		ResourcesPt.Builder _builder = ResourcesPt.newBuilder();
		_builder.setType(ResConstants.RES);
		_builder.setId(ResConstants.GOLD);
		_builder.setNum(glod);
		builder.addResp(_builder);
		player.sendClientPacket(GateServerCode.RESOURCES, builder.build().toByteArray());
	}

	/**
	 * 星球删除
	 * 
	 * @param packet
	 */
	public void remove(Packet packet) {
		GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
		try {
			RemoveRsp.Builder rspBuilder = RemoveRsp.newBuilder();
			RemoveReq builder = RemoveReq.parseFrom(packet.getBody());
			int index = builder.getIndex();
			int heatBallId = getHeatBallIdByIndex(index);
			// 校验数据
			if (heatBallId == 0) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.REMOVE, rspBuilder.build().toByteArray());
				return;
			}

			JSONArray heatBall = getHeatBallData();
			heatBall.set(index, 0);
			setData(HeatBallKey.HEATBALL, heatBall);
			// 返回客户端
			rspBuilder.setIsSucc(true);
			player.sendClientPacket(GateServerCode.REMOVE, rspBuilder.build().toByteArray());
		} catch (InvalidProtocolBufferException e) {
			Log.error(e.getMessage());
		}
	}

	/**
	 * 移动星球
	 * 
	 * @param packet
	 */
	public void move(Packet packet) {

		try {
			int userId = packet.getUserId();
			GamePlayer player = GameMgr.getCachePlayer(userId);
			MoveHeatBallReq reqBuilder = HeatBallProto.MoveHeatBallReq.parseFrom(packet.getBody());

			HeatBallProto.MoveHeatBallRsp.Builder rspBuilder = HeatBallProto.MoveHeatBallRsp.newBuilder();
			// 检查离线收益金币
			if (!player.checkOffLineGold()) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("非法操作");
				player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
				return;
			}
			int sourceIndex = reqBuilder.getSourceIndex();
			int targetIndex = reqBuilder.getTargetIndex();
			// 校验参数合法性
			if (sourceIndex < 0 || sourceIndex > HeatBallKey.HEATBALL_MAX_SIZE || targetIndex < 0
					|| targetIndex > HeatBallKey.HEATBALL_MAX_SIZE) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
				return;
			}

			int sourceHeatBallId = getHeatBallIdByIndex(sourceIndex);
			int targetHeatBallId = getHeatBallIdByIndex(targetIndex);
			// 校验是否有星球
			if (sourceHeatBallId == 0) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误，无法移动");
				player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
				return;
			}

			// 如果有走合成
			if (targetHeatBallId > 0) {
				compose(player, sourceHeatBallId, targetHeatBallId, sourceIndex, targetIndex, rspBuilder);
			} else {// 没有走移动位置
				movePos(player, sourceHeatBallId, sourceIndex, targetIndex, rspBuilder);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置战斗展示位
	 * 
	 * @param packet
	 */
	public void battlePos(Packet packet) {
		try {
			BattlePosReq reqBuilder = BattlePosReq.parseFrom(packet.getBody());

			int heatBallId = reqBuilder.getHeatBallId();

			BattlePosRsp.Builder rspBuilder = BattlePosRsp.newBuilder();
			// 校验参数
			if (heatBallId <= 0) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BATTLE_POS, rspBuilder.build().toByteArray());
				return;
			}

			// 校验有效性
			HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
			HeatBallTemplate st = mgr.getTemplateById(heatBallId);
			if (st == null) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BATTLE_POS, rspBuilder.build().toByteArray());
				return;
			}

			// 校验是否解锁
			int tujian = getTuJian();
			if (heatBallId > tujian) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("该星球图鉴未解锁");
				player.sendClientPacket(GateServerCode.BATTLE_POS, rspBuilder.build().toByteArray());
				return;
			}
			// 返回客户端
			rspBuilder.setIsSucc(true);
			player.sendClientPacket(GateServerCode.BATTLE_POS, rspBuilder.build().toByteArray());

			// 更新数据
			setData(HeatBallKey.BATTLE_POS, heatBallId);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移动位置
	 * 
	 * @param player
	 * @param sourceheatBallId
	 * @param sourceIndex
	 * @param targetIndex
	 * @param rspBuilder
	 */
	public void movePos(GamePlayer player, int sourceHeatBallId, int sourceIndex, int targetIndex,
			HeatBallProto.MoveHeatBallRsp.Builder rspBuilder) {
		JSONArray heatBall = getHeatBallData();
		// 原位置清掉
		heatBall.set(sourceIndex, 0);
		// 复制到目标位置
		heatBall.set(targetIndex, sourceHeatBallId);
		setData(HeatBallKey.HEATBALL, heatBall);
		// 返回客户端
		rspBuilder.setIsSucc(true);
		rspBuilder.setSysTime(TimeUtil.getCurSecs());
		HeatBallPt.Builder _builder = HeatBallPt.newBuilder();
		_builder.setIndex(targetIndex);
		_builder.setHeatBallId(sourceHeatBallId);
		rspBuilder.setHeatBall(_builder);
		player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
	}

	/**
	 * 合成
	 * 
	 * @param player
	 * @param sourceheatBallId
	 * @param targetheatBallId
	 * @param sourceIndex
	 * @param targetIndex
	 * @param rspBuilder
	 */
	private void compose(GamePlayer player, int sourceHeatBallId, int targetHeatBallId, int sourceIndex,
			int targetIndex, HeatBallProto.MoveHeatBallRsp.Builder rspBuilder) {
		// 校验合成等级
		if (sourceHeatBallId != targetHeatBallId) {
			rspBuilder.setIsSucc(false);
			rspBuilder.setErrMess("星球等级不同，无法合成");
			player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
			return;
		}
		HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
		// 进行合成逻辑
		int newId = sourceHeatBallId + 1;
		BaseTemplate template = (BaseTemplate) mgr.getTemlateById(newId);
		if (template == null) {
			rspBuilder.setIsSucc(false);
			rspBuilder.setErrMess("你的星球以及达到最大等级了，无法再合成了");
			player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
			return;
		}

		// 合成逻辑
		JSONArray heatBall = getHeatBallData();
		heatBall.set(sourceIndex, 0);
		heatBall.set(targetIndex, newId);
		setData(HeatBallKey.HEATBALL, heatBall);
		// 解锁图鉴
		int tujian = getTuJian();
		if (newId > tujian) {
			setData(HeatBallKey.TUJIAN, newId);
		}

		// 返回客户端
		rspBuilder.setIsSucc(true);
		rspBuilder.setSysTime(TimeUtil.getCurSecs());
		HeatBallPt.Builder _builder = HeatBallPt.newBuilder();
		_builder.setIndex(targetIndex);
		_builder.setHeatBallId(newId);
		rspBuilder.setHeatBall(_builder);
		player.sendClientPacket(GateServerCode.MOVE_HEATBALL, rspBuilder.build().toByteArray());
	}

	/**
	 * 碎片合成
	 * 
	 * @param packet
	 */
	public void fragmentCompose(Packet packet) {
		try {
			FragmentComposeReq reqBuilder = FragmentComposeReq.parseFrom(packet.getBody());
			FragmentComposeRsp.Builder rspBuilder = FragmentComposeRsp.newBuilder();
			int heatBallId = reqBuilder.getHeatBallId();
			HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
			HeatBallTemplate template = mgr.getTemplateById(heatBallId);
			// 校验如果为null或者不是稀有皮肤
			if (template == null || template.getType() != 2) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数无效");
				player.sendClientPacket(GateServerCode.FRAGMENT_COMPOSE, rspBuilder.build().toByteArray());
				return;
			}
			int userId = packet.getUserId();

			GamePlayer player = GameMgr.getCachePlayer(userId);
			FragmentInventory fi = player.getModule(FragmentInventory.class);
			int num = fi.getData(Integer.toString(template.getComposeId()));
			// 校验数量
			if (num < template.getComposeNum()) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("碎片不足，无法合成");
				player.sendClientPacket(GateServerCode.FRAGMENT_COMPOSE, rspBuilder.build().toByteArray());
				return;
			}
			Set<Integer> set = getData(HeatBallKey.SPECIAL);
			set.add(heatBallId);
			// 扣除碎片
			fi.addData(Integer.toString(template.getComposeId()), -template.getComposeNum());
			// 返回客户端
			rspBuilder.setIsSucc(true);
			rspBuilder.setHeatBallId(heatBallId);
			player.sendClientPacket(GateServerCode.FRAGMENT_COMPOSE, rspBuilder.build().toByteArray());
			int time = TimeUtil.getCurSecs();
			pushRes(player, ResConstants.GOODS, template.getComposeId(), fi.getDoubleData(Integer.toString(template.getComposeId())), time);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图鉴
	 * 
	 * @param packet
	 */
	public void tuJian(Packet packet) {
		GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
		HeatBallProto.TuJianRsp.Builder builder = TuJianRsp.newBuilder();
		int tujian = getTuJian();
		builder.setIsSucc(true); 
		builder.setHeatBallId(tujian);
		Set<Integer> set = getData(HeatBallKey.SPECIAL);
		Iterator<Integer> iter = set.iterator();
		while(iter.hasNext()) {
			int id = iter.next();
			builder.addSpecialId(id);
		}
		player.sendClientPacket(GateServerCode.TUJIAN, builder.build().toByteArray());
	}

	/**
	 * 获得图鉴
	 * 
	 * @return
	 */
	public int getTuJian() {
		int tujian = getIntData(HeatBallKey.TUJIAN);

		if (tujian == 0) {
			tujian = 1;
			setData(HeatBallKey.TUJIAN, tujian);
		}
		return tujian;
	}

	/**
	 * 获得展示位
	 * 
	 * @return
	 */
	public int getBattlePos() {
		if (getIntData(HeatBallKey.BATTLE_POS) == 0) {
			setData(HeatBallKey.BATTLE_POS, 1);
		}
		return getIntData(HeatBallKey.BATTLE_POS);
	}

	/**
	 * 通过下标获得星球
	 * 
	 * @param index
	 * @return
	 */
	public int getHeatBallIdByIndex(int index) {
		JSONArray heatBall = getHeatBallData();
		if (index < 0 || index > heatBall.size() - 1)
			return 0;
		return heatBall.getIntValue(index);
	}

	/**
	 * 购买星球
	 * 
	 * @param packet
	 */
	public void buy(Packet packet) {

		try {
			HeatBallProto.BuyHeatBallReq reBuilder = HeatBallProto.BuyHeatBallReq.parseFrom(packet.getBody());
			HeatBallProto.BuyHeatBallRsp.Builder builder = HeatBallProto.BuyHeatBallRsp.newBuilder();
			int userId = packet.getUserId();
			int heatBallId = reBuilder.getHeatBallId();
			GamePlayer player = GameMgr.getCachePlayer(userId);
			// 校验星球id
			if (heatBallId <= 0) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 校验是否有该配置的星球数据
			HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
			HeatBallTemplate template = mgr.getTemplateById(heatBallId);
			if (template == null) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 校验参数合法性
			int tujian = getTuJian();
			if (heatBallId > tujian || tujian - heatBallId > diamondBuy) {
				builder.setIsSucc(false);
				builder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 检查离线收益金币
			if (!player.checkOffLineGold()) {
				builder.setIsSucc(false);
				builder.setErrMess("非法操作");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 拿到星球数据和配置数据
			JSONArray heatBall = getHeatBallData();
			// 校验玩家星球格子
			int index = getEmptyIndex(heatBall);
			if (index < 0) {
				builder.setIsSucc(false);
				builder.setErrMess("格子不足~");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			int buyTimes = getBuyTimes(heatBallId);
			int _buyTimes = buyTimes + 1;
			// 计算拿到价格
			double price = getPrice(_buyTimes, heatBallId);
			if (price == 0) {
				builder.setIsSucc(false);
				builder.setErrMess("非法操作");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 拿到玩家金币
			double playerGlod = player.getDoubleData(PlayerKey.GOLD);

			// 校验玩家金币
			if (playerGlod < price) {
				builder.setIsSucc(false);
				builder.setErrMess("金币不足~");
				player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
				return;
			}

			// 执行正常购买逻辑
			heatBall.set(index, heatBallId);
			setData(HeatBallKey.HEATBALL, heatBall);
			setBuyTimes(heatBallId, _buyTimes);
			// 扣钱
			player.addData(PlayerKey.GOLD, -price);

			int time = TimeUtil.getCurSecs();
			// 返回客户端
			sendBuyResult(player, index, heatBallId, time, _buyTimes);
			// 推送客户端资源变化
			pushRes(player, ResConstants.RES, ResConstants.GOLD, player.getDoubleData(PlayerKey.GOLD), time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 计算离线收益
	 * 
	 * @param player
	 * @return
	 */
	public double calcLionOffIncome(GamePlayer player) {

		JSONArray heatBall = getHeatBallData();
		int now = TimeUtil.getCurSecs();
		if (player.getOffLineSecs() == 0)
			return 0;
		// 拿到离线时长
		int lionOffTime = now - player.getOffLineSecs();
		if (lionOffTime > logOffIncome) {
			lionOffTime = logOffIncome;
		}
		// 计算最大离线金币数量
		HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);

		// 当前时间-上次收益时间/收益评率*收益金币数
		double result = 0;
		// 便利所有星球，计算收益
		for (int i = 0; i < HeatBallKey.HEATBALL_MAX_SIZE; i++) {
			int heatBallId = heatBall.getInteger(i);
			if (heatBallId == 0)
				continue;

			HeatBallTemplate template = mgr.getTemplateById(heatBallId);
			if (template == null) {
				Log.error("heatBallTemplate is null for id:" + heatBallId);
				continue;
			}
			result += lionOffTime / template.getIncomeTime() * template.getGoldIncome();
		}
		// 更新收益结算时间
		setData(HeatBallKey.INCOME_TIME, now);
		// 更新玩家资源
		player.setData(PlayerKey.OFF_LINE_GOLD, result);
		// 记录日志
		Log.info("lionOffCalcIncome glod:" + result);
		return result;

	}

	/**
	 * 计算收益
	 * 
	 * @param player
	 * @return
	 */
	public double calcIncome(GamePlayer player) {
		JSONArray heatBall = getHeatBallData();
		// 拿到上次收益时间
		int incomeTime = getIntData(HeatBallKey.INCOME_TIME);
		// 如果等于0代表新号
		if (incomeTime == 0) {
			incomeTime = TimeUtil.getCurSecs() - INCOME_TIME;
		}
		int nowTime = TimeUtil.getCurSecs();

		HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);

		// 取得buff
		BuffInventory bi = player.getModule(BuffInventory.class);

		Map<Integer, Integer> buffMap = bi.getBuff();
		// 便利所有星球，计算收益
		double result = 0;
		for (int i = 0; i < HeatBallKey.HEATBALL_MAX_SIZE; i++) {
			int heatBallId = heatBall.getInteger(i);
			if (heatBallId == 0)
				continue;

			HeatBallTemplate template = mgr.getTemplateById(heatBallId);
			if (template == null) {
				Log.error("heatBallTemplate is null for id:" + heatBallId);
				continue;
			}

			// 当前时间-上次收益时间/收益评率*收益金币数
			// 为了做容错，增加1s
			int time = (nowTime - incomeTime) + 1;

			if (buffMap == null || buffMap.isEmpty()) {
				result += time / template.getIncomeTime() * template.getGoldIncome();
			} else {
				Iterator<Entry<Integer, Integer>> iter = buffMap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Integer> en = iter.next();
					result += time / template.getIncomeTime() * template.getGoldIncome() * en.getValue();
				}
			}
		}
		// 更新收益结算时间
		setData(HeatBallKey.INCOME_TIME, nowTime);
		// 更新玩家资源
		player.addDoubleData(PlayerKey.GOLD, result);
		// 记录日志
		Log.info("playerId:" + player.getUserId() + ",calcIncome glod:" + result);
		return player.getDoubleData(PlayerKey.GOLD);
	}

	/**
	 * 购买返回
	 * 
	 * @param player
	 * @param index
	 * @param heatBallId
	 */
	private void sendBuyResult(GamePlayer player, int index, int heatBallId, int time, int buyTimes) {
		HeatBallProto.BuyHeatBallRsp.Builder builder = HeatBallProto.BuyHeatBallRsp.newBuilder();
		builder.setIsSucc(true);
		HeatBallPt.Builder _builder = HeatBallPt.newBuilder();
		_builder.setIndex(index);
		_builder.setHeatBallId(heatBallId);
		builder.setHeatBall(_builder);
		builder.setSysTime(time);
		builder.setBuyTimes(buyTimes);
		player.sendClientPacket(GateServerCode.BUY_HEATBALL, builder.build().toByteArray());
	}

	/**
	 * 推送资源
	 * 
	 * @param player
	 * @param glod
	 */
	private void pushRes(GamePlayer player, int type, int id, double num, int time) {
		ResoucesRsp.Builder resBulider = ResoucesRsp.newBuilder();
		BaseProto.ResourcesPt.Builder resPtBuilder = BaseProto.ResourcesPt.newBuilder();
		resPtBuilder.setType(type);
		resPtBuilder.setId(id);
		resPtBuilder.setNum(num);
		resBulider.setSysTime(time);
		resBulider.addResp(resPtBuilder);
		player.sendClientPacket(GateServerCode.RESOURCES, resBulider.build().toByteArray());
	}

	/**
	 * 获取玩家星球空位置
	 * 
	 * @param heatBall
	 * @return
	 */
	private int getEmptyIndex(JSONArray heatBall) {
		for (int i = 0; i < heatBall.size(); i++) {
			if (heatBall.getIntValue(i) != 0)
				continue;
			return i;
		}
		return -1;
	}

	/**
	 * 是否全部解锁了
	 * 
	 * @return
	 */
	public boolean isDeblockAll() {
		int tujianId = getTuJian();
		HeatBallTemplateMgr mgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
		if (mgr.getMaxId() == tujianId)
			return true;
		return false;
	}

	/**
	 * 通过公式获得购买价格 其中有金币购买和钻石购买，计算公式不一样
	 * 
	 * @param buyTimes
	 * @return
	 */
	private double getPrice(int buyTimes, int headBallId) {
		double result = 0;
		int tujian = getTuJian();
		int level = tujian - headBallId;
		// 优先钻石金币购买
		if (level == diamondBuy) {
			result = headBallId + 2 * buyTimes * headBallId;
		} else if (level >= goldBuy) {// 金币购买
			if (headBallId - goldBuy <= 0)
				headBallId = 1;
			result = base + (Math.pow(2, headBallId + addend) + buyTimes - subtrahend1) * multiplier
					* (Math.pow(2, headBallId + addend) + buyTimes - subtrahend2) / divisor;
		}
		// 取整，把小数点去掉
		result = Math.floor(result);
		return result;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendSome(Object... params) {
		// TODO Auto-generated method stub

	}

}
