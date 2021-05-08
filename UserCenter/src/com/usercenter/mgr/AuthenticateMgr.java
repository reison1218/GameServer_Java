package com.usercenter.mgr;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.usercenter.authenticate.Authenticate;
import com.usercenter.authenticate.SteamAuthenticate;
import com.usercenter.base.config.Config;
import com.usercenter.base.config.ConfigKey;

public class AuthenticateMgr {



	/** 赛季管理器 **/
	private static Map<String, Authenticate> authMap = new HashedMap<>();

	public static boolean init() {
		SteamAuthenticate st = new SteamAuthenticate();
		st.init();
		authMap.put(ConfigKey.STEAM_CONF, st);
		String res = st.authenticateUserTicket("test");
		System.out.println(res);
		return true;
	}
	
	
	public static Authenticate getAuthenticate(String key) {
		return authMap.get(key);
	}
	
}
