package com.base.template;

import java.util.ArrayList;
import java.util.List;

import com.base.template.base.BaseTemplate;

import io.netty.util.internal.StringUtil;
import proto.BaseProto.ResourcesPt;

/**
 * 签到配置表
 * 
 * @author reison
 *
 */
public class SignInTemplate extends BaseTemplate {

	/** 签到id(天) **/
	private int id;

	/** 签到奖励 **/
	private String reward;

	private List<ResourcesPt.Builder> rewardList = new ArrayList<ResourcesPt.Builder>();
	
	

	@Override
	public void initStr() {
		if (StringUtil.isNullOrEmpty(this.reward)) {
			return;
		}
		String[] strs = this.reward.split(";");
		for (String s : strs) {
			int id = Integer.parseInt(s.split("_")[0]);
			double num = Double.parseDouble(s.split("_")[1]);
			ResourcesPt.Builder builder = ResourcesPt.newBuilder();
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

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Object getId() {
		return id;
	}

}
