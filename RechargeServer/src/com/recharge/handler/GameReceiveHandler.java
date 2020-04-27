package com.recharge.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.utils.Log;
import com.recharge.entity.PayOrderDao;

public class GameReceiveHandler extends AbstractHandler {
	public static int SUCCESS = 200;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/pay/gameOrderReceive".equals(target)) {
			Log.info("target!=/pay/gameOrderReceive");
			return;
		}
		Log.info("游戏服回调的接口，接收订单信息" + request);
		try {
			if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
				Log.error("参数为null!");
				return;
			}
			int gameId = Integer.parseInt(request.getParameterMap().get("game_id")[0]);
			double ourNo = Double.parseDouble(request.getParameterMap().get("out_trade_no")[0]);
			int state = Integer.parseInt(request.getParameterMap().get("state")[0]);
			if (state != SUCCESS) {
				Log.info("游戏服：" + gameId + " 处理订单号失败！请检查！");
				return;
			}
			// 修改订单状态
			PayOrderDao.getInstance().findByOutTradeNo(ourNo);

		} catch (Exception e) {
			Log.error(e.getMessage());
		}

	}
}
