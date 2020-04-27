package com.base.template;

import java.util.ArrayList;
import java.util.List;

import com.base.template.base.BaseTemplate;

import io.netty.util.internal.StringUtil;
import proto.BaseProto.ResourcesPt;

public class AdvertInComeTemplate extends BaseTemplate {

	/** 资源 **/
	public static final int RES = 1;

	/** 倍数 **/
	public static final int MULTIPLE = 2;

	/** 复活 **/
	public static final int REVIVE = 3;

	private int id;

	private int type;

	private String reward;

	private List<ResourcesPt.Builder> rewardList = new ArrayList<ResourcesPt.Builder>();

	public int getMultiple() {
		if (type != MULTIPLE)
			return 1;
		return Integer.parseInt(reward);
	}

	public int getRevive() {
		if (type != REVIVE)
			return 0;
		return Integer.parseInt(reward);
	}

	public List<ResourcesPt.Builder> getRewardList() {
		return rewardList;
	}

	@Override
	public Object getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getReward() {
		return reward;
	}

	@Override
	public void initStr() {
		if (StringUtil.isNullOrEmpty(this.reward) || type != RES) {
			return;
		}

		if (this.reward.contains(";")) {
			String[] strs = this.reward.split(";");
			for (String s : strs) {
				int id = Integer.parseInt(s.split("_")[0]);
				double num = Double.parseDouble(s.split("_")[1]);
				ResourcesPt.Builder builder = ResourcesPt.newBuilder();
				builder.setId(id);
				builder.setNum(num);
				rewardList.add(builder);
			}
		} else {
			int id = Integer.parseInt(this.reward.split("_")[0]);
			double num = Double.parseDouble(this.reward.split("_")[1]);
			ResourcesPt.Builder builder = ResourcesPt.newBuilder();
			builder.setId(id);
			builder.setNum(num);
			rewardList.add(builder);
		}

	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public void setId(int id) {
		this.id = id;
	}
}
