package com.usercenter.mgr;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.usercenter.entity.config.GameConfig;
import com.usercenter.entity.config.GameConfigDao;

/**
 * 静态配置管理器
 * @author reison
 *
 */
public class ConfigMgr {
	
	private static ConfigMgr instance = new ConfigMgr();
	
	public static final ConfigMgr getInstance() {
		return instance;
	}
	
	/**游戏配置**/
	private static Map<Integer,GameConfig> gameConfigMap = new HashedMap<>();
	
	/**
	 * 初始化配置信息
	 * @return
	 */
	public  boolean init() {
		gameConfigMap = GameConfigDao.getInstance().findGameConfigs();
		return true;
	}
	
	/**
	 * 获得所有游戏配置
	 * @return
	 */
	public Map<Integer,GameConfig> getAllGameConfigs(){
		return gameConfigMap;
	}

}
