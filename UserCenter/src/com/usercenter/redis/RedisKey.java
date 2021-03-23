package com.usercenter.redis;

/**
 * redis key
 * @author tangjian
 *
 */
public interface RedisKey {
	/**玩家数据**/
	public static final String USERS = "users";
	/**玩家id对应平台id**/
	public static final String UID_2_PID = "uid_2_pid";
	/**玩家名字对应玩家id**/
	public static final String NAME_2_UID = "name_2_uid";
	/**赛季数据**/
	public static final String GAME_SEASON = "game_season";
}
