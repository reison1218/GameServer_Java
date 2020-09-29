package com.usercenter.entity;

import java.util.Date;

import com.utils.TimeUtil;

/**
 * 赛季动态数据
 * 
 * @author reison
 *
 */
public class SeasonInfo {
	/** 游戏id **/
	private int game_id;
	/** 赛季id **/
	private int season_id;
	/** 上次更新赛季时间 **/
	private Date last_update_time;
	/** 上次更新赛季时间 **/
	private Date next_update_time;
	
	public int getGame_id() {
		return game_id;
	}
	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}
	public int getSeason_id() {
		return season_id;
	}
	public void setSeason_id(int season_id) {
		this.season_id = season_id;
	}
	public String getLast_update_time() {
		String str = TimeUtil.getDateFormat(last_update_time);
		return str;
	}
	public void setLast_update_time(Date last_update_time) {
		this.last_update_time = last_update_time;
	}
	public String getNext_update_time() {
		String str = TimeUtil.getDateFormat(next_update_time);
		return str;
	}
	public void setNext_update_time(Date next_update_time) {
		this.next_update_time = next_update_time;
	}
	

	
}
