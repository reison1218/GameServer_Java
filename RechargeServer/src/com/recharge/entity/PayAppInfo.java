package com.recharge.entity;

import java.util.Date;

/**
 * 渠道信息
 * @author reison
 *
 */
public class PayAppInfo {

	/** 主键id **/
	private int id;

	/** 游戏id **/
	private int gameId;

	/** 渠道id **/
	private String channelId;

	/** appid **/
	private String appId;

	/** 密钥 **/
	private String appSecret;

	/** 创建时间 **/
	private Date cTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public Date getcTime() {
		return cTime;
	}

	public void setcTime(Date cTime) {
		this.cTime = cTime;
	}
}
