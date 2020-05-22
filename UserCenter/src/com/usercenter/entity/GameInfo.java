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
	private int game_id;
	/**游戏名字**/
	private String name;
	/**创建时间**/
	private Date create_time;
	/**发布时间**/
	private Date release_time;
	/**更新时间**/
	private Date update_time;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("game_id:");
		sb.append(game_id);
		sb.append(",");
		sb.append("name:");
		sb.append(name);
		sb.append(",");
		sb.append("create_time:");
		sb.append(TimeUtil.getDateFormat(create_time));
		sb.append(",");
		sb.append("release_time:");
		sb.append(TimeUtil.getDateFormat(release_time));
		sb.append(",");
		sb.append("update_time:");
		sb.append(TimeUtil.getDateFormat(update_time));
		return sb.toString();
	}


	public int getGame_id() {
		return game_id;
	}


	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Date getCreate_time() {
		return create_time;
	}


	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}


	public Date getRelease_time() {
		return release_time;
	}


	public void setRelease_time(Date release_time) {
		this.release_time = release_time;
	}


	public Date getUpdate_time() {
		return update_time;
	}


	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}

	
}