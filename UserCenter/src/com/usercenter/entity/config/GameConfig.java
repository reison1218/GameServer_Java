package com.usercenter.entity.config;

/**
 * 游戏相关配置
 * @author reison
 *
 */
public class GameConfig {
	/**游戏id**/
	private int game_id;
	/**默认赛季**/
	private int default_season;
	
	
	public int getGame_id() {
		return game_id;
	}
	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}
	public int getDefault_season() {
		return default_season;
	}
	public void setDefault_season(int default_season) {
		this.default_season = default_season;
	}
}
