package com.base.template;

import com.base.template.base.BaseTemplate;

/**
 * 星球配置模板
 * 
 * @author reison
 *
 */
public class HeatBallTemplate extends BaseTemplate {

	/** 星球id **/
	private int id;
	
	/**类型(1:普通类型 2:稀有类型)**/
	private int type;
	
	/** 需要的合成id **/
	private int composeId;

	/** 所需要的合成数量 **/
	private int composeNum;

	/** 金币收益 **/
	private double goldIncome;
	
	/**奖励**/
	private double reward;
	
	
	/** 收益时间单位（按秒来算） **/
	private int incomeTime;
	
	

	@Override
	public void initStr() {
		
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getComposeId() {
		return composeId;
	}

	public void setComposeId(int composeId) {
		this.composeId = composeId;
	}

	public int getComposeNum() {
		return composeNum;
	}

	public void setComposeNum(int composeNum) {
		this.composeNum = composeNum;
	}

	public double getGoldIncome() {
		return goldIncome;
	}

	public void setGoldIncome(double goldIncome) {
		this.goldIncome = goldIncome;
	}

	public int getIncomeTime() {
		return incomeTime;
	}

	public void setIncomeTime(int incomeTime) {
		this.incomeTime = incomeTime;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	@Override
	public Object getId() {
		return id;
	}
}
