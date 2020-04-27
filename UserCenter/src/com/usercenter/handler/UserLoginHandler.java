package com.usercenter.handler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import io.netty.util.internal.StringUtil;

import com.usercenter.action.SaveUserInfoAction;
import com.usercenter.base.executor.ExecutorMgr;
import com.usercenter.entity.UserInfo;
import com.usercenter.entity.UserInfoDao;
import com.usercenter.mgr.UserCenterMgr;
import com.usercenter.redis.RedisKey;
import com.usercenter.redis.RedisPool;

public class UserLoginHandler extends AbstractHandler {
	/**成功的状态码**/
	public static int SUCCESS = 200;
	/**一天的秒数**/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/getUserId".equals(target)) {
			Log.info("target!=/center/getUserId");
			return;
		}
		String platformId = null;
		Log.info("用户中心收得到登陆请求，查找用户id，信息:" + request);
		JSONObject jsObject = new JSONObject();
		int resultUserId = 0;
		boolean result = false;
		try {
			response.setHeader("Content-Type","text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			if(request.getMethod().equals("GET")) {
				if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
					Log.error("参数为null!");
					return;
				}
				platformId = request.getParameter("platform_id");
			}else {
				byte[] bytes = new byte[126];
				request.getInputStream().read(bytes);
				String str = new String(bytes);
				JSONObject js = (JSONObject) JsonUtil.parse(str.trim());
				result = js.containsKey("platform_id");
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "platform_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("platform_id param not find!");
					return;
				}
				
				result = js.containsKey("game_id");
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "game_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id param not find!");
					return;
				}
				
				platformId = js.getString("platform_id");
				int gameId = js.getInteger("game_id");
				String registerPlatform = js.getString("register_platform");
				String nickName = js.getString("nick_name");
				String phoneNo = js.getString("phone_no");
				//校验platformId
				if(StringUtils.isEmpty(platformId)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "platform_id is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("platform_id is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(registerPlatform)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "register_platform is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("register_platform is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(nickName)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "nick_name is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("nick_name is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(phoneNo)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "phone_no is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("phone_no is empty!");
					return;
				}
				
				if(gameId <=0) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "game_id is invalid!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id is invalid!");
					return;
				}
				//判断是否存在这个游戏
				result = UserCenterMgr.hasGame(gameId);
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "this game is not exist for game_id:"+gameId);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("this game is not exist for game_id:"+gameId);
					return;
				}
				//校验玩家数据是否存在
				String value = RedisPool.hget(RedisKey.USERS, platformId);
				if(StringUtils.isEmpty(value)) {
					result = RedisPool.exists(RedisKey.USERS);
					UserInfo userInfo = UserInfoDao.getInstance().findUserInfoByPlatformIdAndGameId(platformId,gameId);
					//代表新号,创建新号
					if(userInfo == null) {
						AtomicInteger userId = UserCenterMgr.getMaxUserId(gameId,true);
						resultUserId = userId.incrementAndGet();
						userInfo = new UserInfo();
						userInfo.setCreateTime(TimeUtil.getSysteCurTime());
						userInfo.setUserId(resultUserId);
						userInfo.setGameId(gameId);
						userInfo.setNickName("test");
						userInfo.setPhoneNo("no");
						userInfo.setPlatformId(platformId);
						userInfo.setRealName("test");
						userInfo.setRegisterIp(request.getRemoteAddr());
						userInfo.setRegisterPlatform(registerPlatform);
						ExecutorMgr.getOrderExecutor().enqueue(new SaveUserInfoAction(null, userInfo));
					}else {
						resultUserId = userInfo.getGameId();
					}
					value = JsonUtil.stringify(userInfo);
					int deleteTime = 0;
					if(!result) {
						deleteTime = DAY_SEC*7;
					}
					RedisPool.hset(RedisKey.USERS, platformId, value,deleteTime);
				}else {
					UserInfo userInfo = JsonUtil.parse(value, UserInfo.class);
					resultUserId = userInfo.getGameId();
				}
			}
			
			Log.info("platformId:"+platformId);
			jsObject.put("status", "OK");
			jsObject.put("user_id", resultUserId);
			response.setStatus(SUCCESS, "OK");
			response.setHeader("Content-Type","text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getOutputStream().write(jsObject.toJSONString().getBytes());
		} catch (Exception e) {
			Log.error(e.getMessage());
		}finally {
			request.getInputStream().close();
			response.getOutputStream().close();
		}
	}
}
