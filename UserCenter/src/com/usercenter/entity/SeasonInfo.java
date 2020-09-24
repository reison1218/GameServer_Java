package com.usercenter.entity;

import java.util.Date;

/**
 * 赛季动态数据
 * 
 * @author reison
 *
 */
public class SeasonInfo {
	/** 游戏id **/
	private int gameId;
	/** 赛季id **/
	private int seasonId;
	/** 上次更新赛季时间 **/
	private Date lasUpdateTime;
	
	
	public int getGameId() {
		return gameId;
	}
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	public int getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(int seasonId) {
		this.seasonId = seasonId;
	}
	public Date getLasUpdateTime() {
		return lasUpdateTime;
	}
	public void setLasUpdateTime(Date lasUpdateTime) {
		this.lasUpdateTime = lasUpdateTime;
	}
}
