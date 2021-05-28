package com.usercenter.handler;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
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
				byte[] bytes = new byte[2048];
				request.getInputStream().read(bytes);
				String str = new String(bytes);
				JSONObject js = (JSONObject) JsonUtil.parse(str.trim());

				String registerPlatform;
				String platformValue;
				String nickName;
				String phoneNo;
				int gameId;
				String platformId = null;

				boolean isDebug = Config.getConfig(ConfigKey.DEBUG);

				registerPlatform = js.getString("register_platform");
				platformValue = js.getString("platform_value");
				gameId = js.getInteger("game_id");
				nickName = js.getString("nick_name");
				phoneNo = js.getString("phone_no");

				// 校验参数
				Result paramsResult = checkParams(gameId, registerPlatform, platformValue, nickName, phoneNo);
				if (paramsResult.httpCode != 200) {
					response.setStatus(paramsResult.httpCode);
					jsObject.put("err_mess", paramsResult.errString);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error(paramsResult.errString);
					return;
				}

				if (!isDebug) {
					// 如果是steam
					if (registerPlatform.equals(ConfigKey.STEAM_CONF)) {

						paramsResult = authenticateSteam(registerPlatform, platformValue);
						if (paramsResult.httpCode != 200) {
							response.setStatus(paramsResult.httpCode);
							jsObject.put("err_mess", paramsResult.errString);
							jsObject.put("status", "fail!");
							response.getOutputStream().write(jsObject.toJSONString().getBytes());
							Log.error(paramsResult.errString);
							return;
						}
						platformId = paramsResult.successValue;
					}
				} else {
					platformId = platformValue;
				}
				// 校验平台id是否是空的
				if (platformId == null) {
					String errStr = "params is error!";
					response.setStatus(500);
					jsObject.put("err_mess", errStr);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error(errStr);
					return;
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
						response.setStatus(411);
						jsObject.put("err_mess", "nick_name is repeated!");
						jsObject.put("status", "fail!");
						response.getOutputStream().write(jsObject.toJSONString().getBytes());
						Log.error("nick_name is repeated!");
						return;
					}
					// 创建新玩家
					userInfo = createUser(gameId, nickName, platformId, phoneNo, request.getRemoteAddr(),
							registerPlatform);
				} else {
					// 反序列化成UserInfo对象
					userInfo = JsonUtil.parse(value, UserInfo.class);
				}
				// 判断是否在线
				if (userInfo.isOn_line() && isDebug) {
					response.setStatus(412);
					jsObject.put("err_mess", "this account already login!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("repeat login for pid:" + platformId);
					return;
				}
				// 如果名字不一样，就改名字
				paramsResult = checkNickName(userInfo, platformId, nickName);
				if (paramsResult.httpCode != 200) {
					response.setStatus(paramsResult.httpCode);
					jsObject.put("err_mess", paramsResult.errString);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error(paramsResult.errString);
					return;
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
	 * 创建新用户
	 * 
	 * @param userInfo
	 * @param gameId
	 * @param nickName
	 * @param platformId
	 * @param phoneNo
	 * @param RegisterIp
	 * @param registerPlatform
	 */
	public UserInfo createUser(int gameId, String nickName, String platformId, String phoneNo, String RegisterIp,
			String registerPlatform) {
		String nickNameLowerCase = nickName.toLowerCase();
		// 代表新号,创建新号
		AtomicInteger userId = UserCenterMgr.getMaxUserId(gameId, true);
		int resultUserId = userId.incrementAndGet();
		String ruId = Integer.toString(resultUserId);
		UserInfo userInfo = new UserInfo();
		Date date = TimeUtil.getSysteCurTime();
		userInfo.setCreate_time(date);
		userInfo.setUser_id(resultUserId);
		userInfo.setGame_id(gameId);
		userInfo.setNick_name(nickName);
		userInfo.setPhone_no(phoneNo);
		userInfo.setPlatform_id(platformId);
		userInfo.setRegister_ip(RegisterIp);
		userInfo.setRegister_platform(registerPlatform);
		userInfo.setLast_login_time(date);
		userInfo.setOn_line(false);
		// 异步保存玩家账号数据到db
		ExecutorMgr.getOrderExecutor().enqueue(new SaveUserInfoAction(null, userInfo));
		// 序列化成json
		String value = JsonUtil.stringify(userInfo);
		// 持久化玩家数据
		RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.USERS, platformId, value, 0);
		// 持久化玩家id对应平台id
		RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.UID_2_PID, ruId, platformId, 0);
		// 持久化名字对应玩家id
		RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID, nickNameLowerCase, ruId, 0);
		return userInfo;
	}

	/**
	 * 检查名字
	 * 
	 * @param userInfo
	 * @param platformId
	 * @param nickName
	 * @return
	 */
	public Result checkNickName(UserInfo userInfo, String platformId, String nickName) {
		Result result = new Result();
		result.httpCode = 200;
		String oldName= userInfo.getNick_name();
		// 如果名字不一样，就改名字
		if (oldName.equals(nickName)) {
			return result;
		}

		String nickNameLowerCase = nickName.toLowerCase();
		// 判断名字是否重复
		String name_2_uid = RedisPool.hgetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID, nickNameLowerCase);
		// 如果有名字了，不允许叫这个名字
		if (!StringUtils.isEmpty(name_2_uid)) {
			result.httpCode = 411;
			result.errString = "nick_name is repeated!nickName:" + nickNameLowerCase;
			return result;
		}
		userInfo.setNick_name(nickName);
		// 序列化成json
		String value = JsonUtil.stringify(userInfo);
		// 持久化玩家数据
		RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.USERS, platformId, value, 0);
		// 删除玩家老名字
		RedisPool.hdel(RedisIndex.USERS,RedisKey.NAME_2_UID, oldName.toLowerCase());
		// 持久化名字对应玩家id
		RedisPool.hsetWithIndex(RedisIndex.USERS, RedisKey.NAME_2_UID, nickNameLowerCase,
				Integer.toString(userInfo.getUser_id()), 0);
		return result;
	}

	/**
	 * 验证steam玩家信息
	 * 
	 * @param registerPlatform
	 * @param platformValue
	 * @return
	 */
	public Result authenticateSteam(String registerPlatform, String platformValue) {
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
		String resJson = steamAuth.authenticateUserTicket(ticket);
		if (resJson == null) {
			String errStr = StringUtils.format("time out!", registerPlatform);
			result.errString = errStr;
			result.httpCode = 500;
			return result;
		}

		JSONObject jsonObject = (JSONObject) JsonUtil.parse(resJson);
		JSONObject jsResponse = (JSONObject) jsonObject.get("response");
		JSONObject jsParams = (JSONObject) jsResponse.get("params");
		if (jsParams == null) {
			String errStr = "invalid ticket";
			result.errString = errStr;
			result.httpCode = 409;
			return result;
		}

		String steamIdStr = jsParams.getString("steamid");
		// 拿到steamid
		long steamId = Long.parseLong(steamIdStr);

		// 校验玩家是有拥有此appid
		resJson = steamAuth.checkAppOwnerShip(steamId);
		jsonObject = (JSONObject) JsonUtil.parse(resJson);
		JSONObject jsAppownership = (JSONObject) jsonObject.get("appownership");
		boolean ownsapp = jsAppownership.getBoolean("ownsapp");

		if (!ownsapp) {
			String errStr = "this player do not have this app!ownsapp is false!register_platform:{0}";
			result.errString = errStr;
			result.httpCode = 410;
			return result;
		}
		result.successValue = steamId + "";
		result.httpCode = 200;
		return result;
	}

	private Result checkParams(int gameId, String registerPlatform, String platformValue, String nickName,
			String phoneNo) {
		Result result = new Result();
		// 校验注册平台
		if (StringUtils.isEmpty(registerPlatform)) {
			result.httpCode = 400;
			result.errString = "register_platform is empty!";
			return result;
		}

		// 校验平台参数
		if (StringUtils.isEmpty(platformValue)) {
			result.httpCode = 401;
			result.errString = "platformValue is empty!";
			return result;
		}

		if (StringUtils.isEmpty(nickName)) {
			result.httpCode = 402;
			result.errString = "nick_name is empty!";
			return result;
		}

		if (phoneNo == null) {
			result.httpCode = 403;
			result.errString = "phone_no is empty!";
			return result;
		}

		if (gameId <= 0) {
			result.httpCode = 405;
			result.errString = "game_id is invalid!";
			return result;
		}
		// 判断是否存在这个游戏
		boolean res = UserCenterMgr.hasGame(gameId);
		if (!res) {
			result.httpCode = 406;
			result.errString = "this game is not exist for game_id:" + gameId;
			return result;
		}
		// 校验名称格式合法性
		boolean nickNameRes = StringUtils.validateStrEnglishAnaNum(nickName);
		if (!nickNameRes) {
			result.httpCode = 407;
			result.errString = "this nickName is illegal!nickName:" + nickName;
			return result;
		}
		int nickNameSize = nickName.length();
		// 校验名称长度
		if (nickNameSize < 3) {
			result.httpCode = 408;
			result.errString = "this size of nickName is error! size:" + nickNameSize;
			return result;
		}

		result.httpCode = 200;
		return result;
	}

	class Result {
		public int httpCode;
		public String errString;
		public String successValue;
	}
}
