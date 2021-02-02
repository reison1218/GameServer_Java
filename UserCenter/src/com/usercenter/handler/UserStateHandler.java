package com.usercenter.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.usercenter.entity.UserInfo;
import com.usercenter.mgr.UserCenterMgr;
import com.usercenter.redis.RedisIndex;
import com.usercenter.redis.RedisKey;
import com.usercenter.redis.RedisPool;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

public class UserStateHandler extends AbstractHandler {
	/** 成功的状态码 **/
	public static int SUCCESS = 200;
	/** 一天的秒数 **/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/user_state".equals(target)) {
			Log.info("target!=/center/user_state");
			return;
		}
		Log.info("用户中心收得到状态请求，查找用户id，信息:" + request);
		JSONObject jsObject = new JSONObject();
		int resultUserId = 0;
		boolean result = false;
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

				result = js.containsKey("game_id");
				if (!result) {
					response.setStatus(500);
					jsObject.put("err_mess", "game_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id param not find!");
					return;
				}

				result = js.containsKey("user_id");

				if (!result) {
					response.setStatus(500);
					jsObject.put("err_mess", "user_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("user_id param not find!");
					return;
				}

				result = js.containsKey("type");
				if (!result) {
					response.setStatus(500);
					jsObject.put("err_mess", "type param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("type param not find!");
					return;
				}

				int userId = js.getInteger("user_id");
				int gameId = js.getInteger("game_id");
				
				boolean type = js.getBoolean("type");
				if (gameId <= 0) {
					response.setStatus(500);
					jsObject.put("err_mess", "game_id is invalid!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id is invalid!");
					return;
				}
				// 判断是否存在这个游戏
				result = UserCenterMgr.hasGame(gameId);
				if (!result) {
					response.setStatus(500);
					jsObject.put("err_mess", "this game is not exist for game_id:" + gameId);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("this game is not exist for game_id:" + gameId);
					return;
				}
				
				String value = null;
				UserInfo userInfo = null;
				// 校验玩家数据是否存在
				String uIdString = Integer.toString(userId);
				String pId = RedisPool.hgetWithIndex(RedisIndex.USERS, RedisKey.UID_2_PID, uIdString);
				if(StringUtils.isEmpty(pId)) {
					Log.error("pId is null for uid:"+uIdString);
					return;
				}
				value = RedisPool.hgetWithIndex(RedisIndex.USERS,RedisKey.USERS, pId);
				userInfo = JsonUtil.parse(value, UserInfo.class);
				if (userInfo == null) {
					Log.error("UserInfo is null for uid:"+uIdString);
					return;
				}
				resultUserId = userInfo.getGame_id();
				if (type){
					userInfo.setLast_login_time(TimeUtil.getSysteCurTime());
					userInfo.setOn_line(true);
				}else {
					userInfo.setOn_line(false);
				}
				//更新redis数据库
				value = JsonUtil.stringify(userInfo);
				RedisPool.hsetWithIndex(RedisIndex.USERS,RedisKey.USERS,userInfo.getPlatform_id(), value,0);
				//异步持久化userinfo
				UserCenterMgr.addUserInfo(userInfo);
			}

			jsObject.put("status", "OK");
			jsObject.put("user_id", resultUserId);
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
}
