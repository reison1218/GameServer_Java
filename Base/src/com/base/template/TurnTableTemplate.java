package com.base.template;

import java.util.ArrayList;
import java.util.List;

import com.base.template.base.BaseTemplate;

import io.netty.util.internal.StringUtil;
import proto.BaseProto.ResourcesPt;

/**
 * 转盘配置
 * 
 * @author reison
 *
 */
public class TurnTableTemplate extends BaseTemplate {

	/** 转盘格子id **/
	private int id;
	/** 转盘格子对应的奖励 **/
	private String reward;
	/** 转盘格子对应的概率 **/
	private int probability;

	private List<ResourcesPt.Builder> rewardList = new ArrayList<ResourcesPt.Builder>();

	@Override
	public void initStr() {
		if (StringUtil.isNullOrEmpty(this.reward)) {
			return;
		}
		String[] strs = this.reward.split(";");
		for (String s : strs) {
			int type = Integer.parseInt(s.split("_")[0]);
			int id = Integer.parseInt(s.split("_")[1]);
			double num = Double.parseDouble(s.split("_")[2]);
			ResourcesPt.Builder builder = ResourcesPt.newBuilder();
			builder.setType(type);
			builder.setId(id);
			builder.setNum(num);
			rewardList.add(builder);
		}
	}

	public String getReward() {
		return reward;
	}

	public List<ResourcesPt.Builder> getRewardList() {
		return rewardList;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Object getId() {
		return id;
	}

}
