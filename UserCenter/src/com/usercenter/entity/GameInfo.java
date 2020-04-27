package com.usercenter.entity;

import java.util.Date;

import com.utils.TimeUtil;

/**
 * 游戏封装类
 * @author reison
 *
 */
public class GameInfo {

	/**游戏id**/
	private int gameId;
	/**游戏名字**/
	private String name;
	/**创建时间**/
	private Date createTime;
	/**发布时间**/
	private Date releaseTime;
	/**更新时间**/
	private Date updateTime;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gameId:");
		sb.append(gameId);
		sb.append(",");
		sb.append("name:");
		sb.append(name);
		sb.append(",");
		sb.append("createTime:");
		sb.append(TimeUtil.getDateFormat(createTime));
		sb.append(",");
		sb.append("releaseTime:");
		sb.append(TimeUtil.getDateFormat(releaseTime));
		sb.append(",");
		sb.append("updateTime:");
		sb.append(TimeUtil.getDateFormat(updateTime));
		return sb.toString();
	}
	public int getGameId() {
		return gameId;
	}
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getReleaseTime() {
		return releaseTime;
	}
	public void setReleaseTime(Date releaseTime) {
		this.releaseTime = releaseTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}