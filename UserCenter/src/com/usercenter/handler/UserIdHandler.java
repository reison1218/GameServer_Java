package com.usercenter.handler;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.usercenter.action.SaveUserInfoAction;
import com.usercenter.authenticate.SteamAuthenticate;
import com.usercenter.base.config.Config;
import com.usercenter.base.config.ConfigKey;
import com.usercenter.base.executor.ExecutorMgr;
import com.usercenter.entity.UserInfo;
import com.usercenter.mgr.AuthenticateMgr;
import com.usercenter.mgr.UserCenterMgr;
import com.usercenter.redis.RedisIndex;
import com.usercenter.redis.RedisKey;
import com.usercenter.redis.RedisPool;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

public class UserIdHandler extends AbstractHandler {

	/** 成功的状态码 **/
	public static int SUCCESS = 200;
	/** 一天的秒数 **/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/user_id".equals(target)) {
			return;
		}
		Log.info("用户中心收得到登陆请求，查找用户id，信息:" + request);
		JSONObject jsObject = new JSONObject();
		int resultUserId = 0;
		boolean result = false;
		UserInfo userInfo = null;
		try {
			response.setHeader("Content-Type", "text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			if (request.getMethod().equals("GET")) {
				if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
					Log.error("参数为null!");
					return;
				}
			} else {
				byte[] bytes = new byte[512];
				request.getInputStream().read(bytes);
				String str = new String(bytes);
				JSONObject js = (JSONObject) JsonUtil.parse(str.trim());

				String registerPlatform;
				String platformValue;
				String nickName;
				String phoneNo;
				int gameId;
				String platformId;

				boolean isDebug = Config.getConfig(ConfigKey.DEBUG);

				registerPlatform = js.getString("register_platform");
				platformValue = js.getString("platform_value");
				gameId = js.getInteger("game_id");
				nickName = js.getString("nick_name");
				phoneNo = js.getString("phone_no");

				//校验参数
				Result paramsResult=checkParams(gameId,registerPlatform,platformValue,nickName,phoneNo);
				if(paramsResult.httpCode!=200) {
					response.setStatus(paramsResult.httpCode);
					jsObject.put("err_mess", paramsResult.errString);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error(paramsResult.errString);
					return;
				}
				// 如果是steam
				if (registerPlatform.equals(ConfigKey.STEAM_CONF)) {

					Result authResult = authenticateSteam(registerPlatform, platformValue);
					if (authResult.httpCode != 200) {
						response.setStatus(authResult.httpCode);
						jsObject.put("err_mess", authResult.errString);
						jsObject.put("status", "fail!");
						response.getOutputStream().write(jsObject.toJSONString().getBytes());
						Log.error(authResult.errString);
						return;
					}
				}

				String nickNameLowerCase = nickName.toLowerCase();
				// 校验玩家数据是否存在
				String value = RedisPool.hgetWithIndex(RedisIndex.USERS, RedisKey.USERS, platformId);
				if (StringUtils.isEmpty(value)) {
					// 判断名字是否重复
					String name_2_uid = RedisPool.hgetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID,
							nickNameLowerCase);
					// 如果有名字了，不允许叫这个名字
					if (!StringUtils.isEmpty(name_2_uid)) {
						response.setStatus(500);
						jsObject.put("err_mess", "nick_name is repeated!");
						jsObject.put("status", "fail!");
						response.getOutputStream().write(jsObject.toJSONString().getBytes());
						Log.error("nick_name is repeated!");
						return;
					}
					// 代表新号,创建新号
					AtomicInteger userId = UserCenterMgr.getMaxUserId(gameId, true);
					resultUserId = userId.incrementAndGet();
					String ruId = Integer.toString(resultUserId);
					userInfo = new UserInfo();
					Date date = TimeUtil.getSysteCurTime();
					userInfo.setCreate_time(date);
					userInfo.setUser_id(resultUserId);
					userInfo.setGame_id(gameId);
					userInfo.setNick_name(nickName);
					userInfo.setPhone_no(phoneNo);
					userInfo.setPlatform_id(platformId);
					userInfo.setRegister_ip(request.getRemoteAddr());
					userInfo.setRegister_platform(registerPlatform);
					userInfo.setLast_login_time(date);
					userInfo.setOn_line(false);
					// 异步保存玩家账号数据到db
					ExecutorMgr.getOrderExecutor().enqueue(new SaveUserInfoAction(null, userInfo));
					// 序列化成json
					value = JsonUtil.stringify(userInfo);
					// 持久化玩家数据
					RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.USERS, platformId, value, 0);
					// 持久化玩家id对应平台id
					RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.UID_2_PID, ruId, platformId, 0);
					// 持久化名字对应玩家id
					RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID, nickNameLowerCase, ruId, 0);
				}
				// 反序列化成UserInfo对象
				userInfo = JsonUtil.parse(value, UserInfo.class);
				// 判断是否在线
				if (userInfo.isOn_line()) {
					response.setStatus(500);
					jsObject.put("err_mess", "this account already login!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("repeat login for pid:" + platformId);
					return;
				}
				// 如果名字不一样，就改名字
				if (!userInfo.getNick_name().toLowerCase().equals(nickName)) {

					// 判断名字是否重复
					String name_2_uid = RedisPool.hgetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID,
							nickNameLowerCase);
					// 如果有名字了，不允许叫这个名字
					if (!StringUtils.isEmpty(name_2_uid)) {
						response.setStatus(500);
						jsObject.put("err_mess", "nick_name is repeated!");
						jsObject.put("status", "fail!");
						response.getOutputStream().write(jsObject.toJSONString().getBytes());
						Log.error("nick_name is repeated!");
						return;
					}
					userInfo.setNick_name(nickName);
					// 序列化成json
					value = JsonUtil.stringify(userInfo);
					// 持久化玩家数据
					RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.USERS, platformId, value, 0);
					// 持久化名字对应玩家id
					RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID, nickNameLowerCase,
							userInfo.getUser_id(), 0);
				}

				// 记录日志已经返回客户端消息
				Log.info("platformId:" + platformId);
			}
			jsObject.put("status", "OK");
			jsObject.put("user_id", userInfo.getUser_id());
			response.setStatus(SUCCESS);
			response.setHeader("Content-Type", "text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getOutputStream().write(jsObject.toJSONString().getBytes());
		} catch (Exception e) {
			Log.error(e.getMessage());
		} finally {
			request.getInputStream().close();
			response.getOutputStream().close();
		}
	}

	/**
	 * 验证steam玩家信息
	 * 
	 * @param registerPlatform
	 * @param platformValue
	 * @return
	 */
	private Result authenticateSteam(String registerPlatform, String platformValue) {
		Result result = new Result();
		SteamAuthenticate steamAuth = (SteamAuthenticate) AuthenticateMgr.getAuthenticate(ConfigKey.STEAM_CONF);
		if (steamAuth == null) {
			String errStr = StringUtils.format("register_platform's Authenticate is empty!register_platform:{0}",
					registerPlatform);
			result.errString = errStr;
			result.httpCode = 500;
			return result;
		}
		String ticket = platformValue;
		String res = steamAuth.authenticateUserTicket(ticket);
		// 拿到steamid
		long steamId = 0;

		// 校验玩家是有拥有此appid
		res = steamAuth.checkAppOwnerShip(steamId);
		int appIdTemp = 0;
		int appId = Config.getConfig(ConfigKey.APP_ID);

		if (appIdTemp != appId) {
			String errStr = "this player do not have this app!Authenticate is empty!register_platform:{0}";
			result.errString = errStr;
			result.httpCode = 500;
			return result;
		}
		result.value = steamId + "";
		result.httpCode = 200;
		return result;
	}

	private Result checkParams(int gameId, String registerPlatform, String platformValue, String nickName, String phoneNo) {
		Result result = new Result();
		// 校验注册平台
		if (StringUtils.isEmpty(registerPlatform)) {
			result.httpCode=500;
			result.errString="register_platform is empty!";
			return result;
		}

		// 校验平台参数
		if (StringUtils.isEmpty(platformValue)) {
			result.httpCode=500;
			result.errString="platformValue is empty!";
			return result;
		}

		if (StringUtils.isEmpty(nickName)) {
			result.httpCode=500;
			result.errString="nick_name is empty!";
			return result;
		}

		if (phoneNo == null) {
			result.httpCode=500;
			result.errString="phone_no is empty!";
			return result;
		}

		if (gameId <= 0) {
			result.httpCode=500;
			result.errString="game_id is invalid!";
			return result;
		}
		// 判断是否存在这个游戏
		boolean res = UserCenterMgr.hasGame(gameId);
		if (!res) {
			result.httpCode=500;
			result.errString="this game is not exist for game_id:" + gameId;
			return result;
		}
		result.httpCode = 200;
		return result;
	}

	class Result {
		public int httpCode;
		public String errString;
		public String value;
	}
}
