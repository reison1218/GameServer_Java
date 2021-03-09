package com.usercenter.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.usercenter.mgr.UserCenterMgr;
import com.utils.Log;
import com.utils.StringUtils;

public class StopHandler extends AbstractHandler {
	/** 成功的状态码 **/
	public static int SUCCESS = 200;
	/** 一天的秒数 **/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/stop".equals(target)) {
			return;
		}
		Log.info("用户中心收得到停服请求，信息:" + request);
		JSONObject jsObject = new JSONObject();
		try {
			response.setHeader("Content-Type", "text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			if (request.getMethod().equals("GET")) {
				if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
					Log.error("参数为null!");
					return;
				}
				String key = request.getParameter("key");
				//校验是否为null
				if (StringUtils.isEmpty(key)) {
					response.setStatus(500);
					jsObject.put("err_mess", "key param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id param not find!");
					return;
				}
				//校验参数值
				if(!key.equals("stop_and_save")) {
					response.setStatus(500);
					jsObject.put("err_mess", "key's value is error!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id param not find!");
					return;
				}
				//异步持久化userinfo
				UserCenterMgr.stop();
			}

			jsObject.put("status", "OK");
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
			System.exit(0);
		}
	}
}
