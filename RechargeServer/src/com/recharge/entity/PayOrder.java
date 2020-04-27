package com.recharge.entity;

import java.util.Date;

/**
 * 订单信息
 * @author reison
 *
 */
public class PayOrder {

	/** 主键id **/
	private int id;

	/** 玩家id **/
	private int userId;
	/** 创建时间 **/
	private Date cTime;
	/** 游戏id **/
	private int gameId;
	/** 渠道id **/
	private String channelId;
	/** 第三方平台订单号 **/
	private int transactionId;
	/** 我方订单号 **/
	private double outTradeNo;
	/** 结束时间 **/
	private Date timeEnd;
	/** 总价，单位分 **/
	private int totalFee;
	/** 交易类型 **/
	private String tradeType;
	/** 商品id **/
	private int goodsId;
	/** 微信结果 **/
	private String resultCode;
	/** 订单状态 **/
	private int state;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public Date getcTime() {
		return cTime;
	}
	public void setcTime(Date cTime) {
		this.cTime = cTime;
	}
	public int getGameId() {
		return gameId;
	}
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public int getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	public double getOutTradeNo() {
		return outTradeNo;
	}
	public void setOutTradeNo(double outTradeNo) {
		this.outTradeNo = outTradeNo;
	}
	public Date getTimeEnd() {
		return timeEnd;
	}
	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}
	public int getTotalFee() {
		return totalFee;
	}
	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}
	public String getTradeType() {
		return tradeType;
	}
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}
	public int getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}
