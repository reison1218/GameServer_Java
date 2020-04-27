package com.game.module.impl;

import java.util.Date;

import com.base.constant.ResConstants;
import com.base.key.SignInKey;
import com.base.module.AnnModule;
import com.base.module.JsonDataModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.module.able.Resetable;
import com.base.netty.packet.Packet;
import com.base.template.AdvertInComeTemplate;
import com.base.template.HeatBallTemplate;
import com.base.template.mgr.AdvertInComeTemplateMgr;
import com.base.template.mgr.ConstantTemplateMgr;
import com.base.template.mgr.HeatBallTemplateMgr;
import com.base.template.mgr.TemplateMgr;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.player.GamePlayer;
import com.utils.TimeUtil;

import proto.BaseProto.ResourcesPt;
import proto.UserProto;

/**
 * 签到模块
 * 
 * @author reison
 *
 */
@AnnModule(name = "sign_in", comment = "签到", tableType = TableType.JSON_TABLE_ONLY, type = ModuleType.PLAYER_MODULE)
public class SignInInventory extends JsonDataModule implements Resetable {

	public SignInInventory(GamePlayer player) {
		super(player);
	}

	private double base;

	private double multiplier;

	@Override
	public void initData() {
		// 初始化公式需要的配置
		ConstantTemplateMgr mgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);
		// 先获得基数
		String str = (String) mgr.getValueById("signIn.calTime");
		base = Double.parseDouble(str);
		// 获得乘数
		str = (String) mgr.getValueById("signIn.coefficient");
		multiplier = Double.parseDouble(str);
	}

	@Override
	public boolean dayReset() {
		// 判断现在是不是周一，是就重置签到
		if (TimeUtil.getDayOfWeekIndex() != 1)
			return false;
		setData(SignInKey.SIGN_IN_DAYS, 0);
		return true;
	}

	/**
	 * 获取签到天数
	 * 
	 * @return
	 */
	public int getDays() {
		return getIntData(SignInKey.SIGN_IN_DAYS);
	}

	/**
	 * 获取签到时间
	 * 
	 * @return
	 */
	public int getTime() {
		Date time = getData(SignInKey.SIGN_IN_TIME);
		if (time == null)
			return 0;
		return (int) (time.getTime() / 1000);
	}

	/**
	 * 签到
	 * 
	 * @param packet
	 */
	public void signIn(Packet packet) {

		UserProto.SignInReq.Builder reBuilder = UserProto.SignInReq.newBuilder();
		int advertId = reBuilder.getAdvertId();

		int advertMult = 1;
		AdvertInComeTemplateMgr atMgr = (AdvertInComeTemplateMgr) TemplateMgr
				.getTemlateMgr(AdvertInComeTemplateMgr.class);
		AdvertInComeTemplate template = atMgr.getTemplateById(advertId);
		if (template != null) {
			advertMult = template.getMultiple();
		}

		UserProto.SignInRsp.Builder builder = UserProto.SignInRsp.newBuilder();
		GamePlayer player = GameMgr.getCachePlayer(packet.getUserId());
		ConstantTemplateMgr mgr = (ConstantTemplateMgr) TemplateMgr.getTemlateMgr(ConstantTemplateMgr.class);
		int startTime = Integer.parseInt((String) mgr.getValueById("signIn.startTime"));
		// 校验开始时间
		int nowTime = TimeUtil.getCurSecs();
		if (nowTime < startTime) {
			builder.setIsSucc(false);
			builder.setErrMess("现在还不能签到");
			player.sendClientPacket(GateServerCode.SIGN_IN, builder.build().toByteArray());
			return;
		}

		// 校验签到时间
		Date signTime = getData(SignInKey.SIGN_IN_TIME);
		// 判断是否同一天
		if (TimeUtil.inSameDay(signTime)) {
			builder.setIsSucc(false);
			builder.setErrMess("今天已经签到过");
			player.sendClientPacket(GateServerCode.SIGN_IN, builder.build().toByteArray());
			return;
		}

		// 拿到签到天数
		int signIn = player.getIntData(SignInKey.SIGN_IN_DAYS);
		if (signIn == 7) {
			builder.setIsSucc(false);
			builder.setErrMess("现在还不能签到");
			player.sendClientPacket(GateServerCode.SIGN_IN, builder.build().toByteArray());
			return;
		}
		// 签到
		signIn += 1;
		setData(SignInKey.SIGN_IN_DAYS, signIn);
		setData(SignInKey.SIGN_IN_TIME, new Date());
		builder.setIsSucc(true);
		builder.setSignIn(signIn);
		// 奖励
		HeatBallInventory si = player.getModule(HeatBallInventory.class);
		int tujian = si.getTuJian();
		HeatBallTemplateMgr stMgr = (HeatBallTemplateMgr) TemplateMgr.getTemlateMgr(HeatBallTemplateMgr.class);
		HeatBallTemplate stTemplate = stMgr.getTemplateById(tujian);
		double result = Math.pow(base * multiplier * advertMult, signIn) * stTemplate.getGoldIncome();
//		SignInTemplateMgr sMgr = (SignInTemplateMgr) TemplateMgr.getTemlateMgr(SignInTemplateMgr.class);
//		SignInTemplate template = (SignInTemplate) sMgr.getTemlateById(signIn);
//		for (ResourcesPt.Builder b : template.getRewardList()) {
//			builder.addResp(b);
//		}
		ResourcesPt.Builder rb = ResourcesPt.newBuilder();
		rb.setId(ResConstants.GOLD);
		rb.setNum(result);
		builder.addResp(rb);
		// 发奖
		player.sendClientPacket(GateServerCode.SIGN_IN, builder.build().toByteArray());
		// 给玩家加资源
		player.addRes(ResConstants.RES, ResConstants.GOLD, result, true);
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendSome(Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		return false;
	}

}
