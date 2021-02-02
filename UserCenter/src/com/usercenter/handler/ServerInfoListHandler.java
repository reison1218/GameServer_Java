package com.usercenter.handler;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.usercenter.entity.ServerInfo;
import com.usercenter.mgr.UserCenterMgr;
import com.utils.JsonUtil;
import com.utils.Log;

public class ServerInfoListHandler  extends AbstractHandler {
	/** 成功的状态码 **/
	public static int SUCCESS = 200;
	/** 一天的秒数 **/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/server_list".equals(target)) {
			Log.info("target!=/center/server_list");
			return;
		}
		Log.info("用户中心收得到状态请求，查找用户id，信息:" + request);
		JSONObject jsObject = new JSONObject();
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

				int gameId = js.getInteger("game_id");
				
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
				List<ServerInfo> list = UserCenterMgr.getServerList(gameId);
				if (list != null) {
					//String jsonObject = JsonUtil.stringify(list);
					jsObject.put("server_list", list);
				}
			}

			jsObject.put("status", "OK");
			response.setStatus(SUCCESS);
			response.setHeader("Content-Type", "text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getOutputStream().write(jsObject.toJSONString().trim().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			request.getInputStream().close();
			response.getOutputStream().close();
		}
	}
}
