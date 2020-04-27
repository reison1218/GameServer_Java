package com.game.module.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import com.base.constant.ResConstants;
import com.base.key.BuffKey;
import com.base.module.AnnModule;
import com.base.module.JsonTemDataModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.netty.packet.Packet;
import com.base.template.InComeBuffTemplate;
import com.base.template.mgr.InComeBuffTemplateMgr;
import com.base.template.mgr.TemplateMgr;
import com.game.code.impl.GateServerCode;
import com.game.mgr.GameMgr;
import com.game.player.GamePlayer;

import proto.BaseProto.BuffPt;
import proto.BaseProto.ResourcesPt;
import proto.BuffProto.BuffReq;
import proto.BuffProto.BuffRsp;

/**
 * 收益buff模块
 * 
 * @author reison
 *
 */
@AnnModule(name = "buff", comment = "收益buff", tableType = TableType.JSON_TABLE_TEMP, type = ModuleType.PLAYER_MODULE)
public class BuffInventory extends JsonTemDataModule {

	public BuffInventory(GamePlayer player) {
		super(player);
	}

	@Override
	public void send() {

	}

	@Override
	public void sendSome(Object... params) {

	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void initData() {

	}

	/**
	 * 得到所有有效buff
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getBuff() {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		if (dataTemMap.isEmpty())
			return result;
		Iterator<Entry<Integer, JSONObject>> iter = dataTemMap.entrySet().iterator();
		Date now = new Date();
		InComeBuffTemplateMgr mgr = (InComeBuffTemplateMgr) TemplateMgr.getTemlateMgr(InComeBuffTemplateMgr.class);
		while (iter.hasNext()) {
			Entry<Integer, JSONObject> en = iter.next();
			Date endTime = en.getValue().getDate(BuffKey.END_TIME);
			// 过期了就删除
			if (endTime != null && endTime.getTime() <= now.getTime()) {
				continue;
			}
			InComeBuffTemplate template = (InComeBuffTemplate) mgr.getTemlateById(en.getKey());
			result.put(en.getKey(), template.getMultiple());
		}
		return result;
	}

	/**
	 * 获得buf
	 * 
	 * @param packet
	 */
	public void buff(Packet packet) {
		int userId = packet.getUserId();
		GamePlayer player = GameMgr.getCachePlayer(userId);

		BuffRsp.Builder rspBuilder = BuffRsp.newBuilder();

		// 检查离线收益金币
		if (!player.checkOffLineGold()) {
			rspBuilder.setIsSucc(false);
			rspBuilder.setErrMess("非法操作");
			player.sendClientPacket(GateServerCode.BUFF, rspBuilder.build().toByteArray());
			return;
		}
		try {
			BuffReq reqBuilder = BuffReq.parseFrom(packet.getBody());
			int buffId = reqBuilder.getBuffId();
			// 校验buffid
			if (buffId <= 0) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BUFF, rspBuilder.build().toByteArray());
				return;
			}

			InComeBuffTemplateMgr mgr = (InComeBuffTemplateMgr) TemplateMgr.getTemlateMgr(InComeBuffTemplateMgr.class);
			InComeBuffTemplate template = (InComeBuffTemplate) mgr.getTemlateById(buffId);
			if (template == null) {
				rspBuilder.setIsSucc(false);
				rspBuilder.setErrMess("参数错误");
				player.sendClientPacket(GateServerCode.BUFF, rspBuilder.build().toByteArray());
				return;
			}

			// 校验是否需要消耗
			if (template.getConsumeList() != null) {
				// 校验
				for (ResourcesPt.Builder _b : template.getConsumeList()) {
					double value = player.getDoubleData(ResConstants.getPlayerKey(_b.getId()));
					if (value < _b.getNum()) {
						rspBuilder.setIsSucc(false);
						rspBuilder.setErrMess("资源不够，无法购买");
						player.sendClientPacket(GateServerCode.BUFF, rspBuilder.build().toByteArray());
						return;
					}
				}
				// 扣除
				for (ResourcesPt.Builder _b : template.getConsumeList()) {
					player.addRes(ResConstants.RES,_b.getId(), -_b.getNum(), true);
				}

			}

			Date nowTime = new Date();
			Date startTime = getData(template.getCategory(), BuffKey.START_TIME);
			Date endTime = getData(template.getCategory(), BuffKey.END_TIME);
			// 如果没有数据或者已经过期了就初始化时间
			if (startTime == null || endTime.getTime() < nowTime.getTime()) {
				startTime = nowTime;
				endTime = new Date(nowTime.getTime() + template.getKeepTimeMilliSeconds());
			} else {// 没有过期
				endTime = new Date(endTime.getTime() + template.getKeepTimeMilliSeconds());
			}
			// 封装客户端返回
			BuffPt.Builder ptBuilder = BuffPt.newBuilder();
			ptBuilder.setBuffCategory((int) template.getId());
			ptBuilder.setBuffStartTime((int) (startTime.getTime() / 1000));
			ptBuilder.setBuffEndTime((int) (endTime.getTime() / 1000));
			rspBuilder.setIsSucc(true);
			rspBuilder.setBuff(ptBuilder);
			player.sendClientPacket(GateServerCode.BUFF, rspBuilder.build().toByteArray());
			// 更新时间数据
			setData(template.getCategory(), BuffKey.START_TIME, startTime);
			setData(template.getCategory(), BuffKey.END_TIME, endTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}