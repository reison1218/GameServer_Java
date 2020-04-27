package com.base.mgr;

/**
 * 排行信息封装类
 * 
 * @author reison
 *
 */
public class RankInfo {

	private int rank = -1;

	private int score;

	private int playerId;

	private String nickName;

	private String avatar;

	public RankInfo(int playerId) {
		this.playerId = playerId;
	}

	public void addScore(int score) {
		this.score += score;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

}
