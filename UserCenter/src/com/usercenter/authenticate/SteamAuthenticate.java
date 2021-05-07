package com.usercenter.authenticate;

import java.util.HashMap;
import java.util.Map;

import com.usercenter.base.config.Config;
import com.usercenter.base.config.ConfigKey;
import com.utils.HttpUtil;

/**
 * steam验证类
 * 
 * @author tangjian
 *
 */
public class SteamAuthenticate extends Authenticate {

	public static String AuthenticateUserTicketMethodGet = "https://partner.steam-api.com/ISteamUserAuth/AuthenticateUserTicket/v1/";

	public static String CheckAppOwnershipMethodGet = "https://partner.steam-api.com/ISteamUser/CheckAppOwnership/v2/";

	private int appId = 0;

	private String webApiKey = null;

	/**
	 * 获得玩家steamId
	 * @param ticket
	 * @return
	 */
	public String authenticateUserTicket(String ticket) {
		AuthenticateUserTicketAction auta = (AuthenticateUserTicketAction)this.authActionMap.get(AuthenticateUserTicketMethodGet);
		auta.ticket = ticket;
		String res = auta.execute();
		return res;
	}
	
	/**
	 * 验证玩家是否有该app
	 * @param steamId
	 * @return
	 */
	public String checkAppOwnerShip(int steamId) {
		CheckAppOwnerShipAction auta = (CheckAppOwnerShipAction) this.authActionMap.get(CheckAppOwnershipMethodGet);
		auta.steamId = steamId;
		String res = auta.execute();
		return res;
	}

	@Override
	public void init() {
		appId = Config.getConfig(ConfigKey.STEAM_CONF, ConfigKey.PLAYER_TEST_APP_ID);
		webApiKey = Config.getConfig(ConfigKey.STEAM_CONF, ConfigKey.WEB_API_KEY);
		this.authActionMap.put(AuthenticateUserTicketMethodGet,
				new AuthenticateUserTicketAction(appId, AuthenticateUserTicketMethodGet, webApiKey, null));
		this.authActionMap.put(AuthenticateUserTicketMethodGet,
				new CheckAppOwnerShipAction(appId, CheckAppOwnershipMethodGet, webApiKey, 0));
	}

	class CheckAppOwnerShipAction extends AuthenticateAction {

		String key;
		long steamId;

		public CheckAppOwnerShipAction(int appId, String url, String key, long steamId) {
			super(appId, url);
			this.key = key;
			this.steamId = steamId;
		}

		@Override
		public void init() {
		}

		@Override
		public String execute() {
			String reqUrl = this.url;
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("key", this.key);
			paramMap.put("appid", this.appId);
			paramMap.put("steamid", this.steamId);
			return HttpUtil.doGet(reqUrl.toString(), paramMap);
		}

	}

	class AuthenticateUserTicketAction extends AuthenticateAction {
		public String key;
		public String ticket;

		public AuthenticateUserTicketAction(int appId, String url, String key, String ticket) {
			super(appId, url);
			this.key = key;
			this.ticket = ticket;
		}

		@Override
		public void init() {

		}

		@Override
		public String execute() {
			String reqUrl = this.url;
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("key", this.key);
			paramMap.put("appid", this.appId);
			paramMap.put("ticket", this.ticket);
			return HttpUtil.doGet(reqUrl.toString(), paramMap);
		}

	}
}
