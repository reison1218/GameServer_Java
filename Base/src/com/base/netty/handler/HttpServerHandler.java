/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.handler;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.base.netty.util.HttpMsgUtil;
import com.game.main.GameServer;
import com.utils.Log;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * <pre>
 * http处理handler
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

	/**
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Log.error("处理http请求异常", cause);
		ctx.writeAndFlush(HttpMsgUtil.buildHttpMsg(null, "inner error1", "text/plain; charset=UTF-8"))
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext,
	 *      java.lang.Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpRequest fullReq = (FullHttpRequest) msg;
		try {
			HttpMethod method = fullReq.method();
			// 允许跨域
			if (HttpMethod.OPTIONS == method) {
				final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK);
				response.headers().set("Access-Control-Allow-Origin", "*");
				response.headers().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
				response.headers().set("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				return;
			}
			String ip = HttpMsgUtil.getClientIp(ctx.channel(), fullReq);
			Map<String, Object> params = HttpMsgUtil.parse(fullReq);
			JSONObject jsonObj = new JSONObject(params);
			String url = fullReq.uri();
			// System.out.println("收到http请求,method:" + method + ",url：" + url + ",params:" +
			// jsonObj.toJSONString());

			// 其余管理员命令
			Object result = handleMsgReceived(ctx, jsonObj);
			if (result == null) {
				result = true;
			}
			ctx.writeAndFlush(HttpMsgUtil.buildHttpMsg(fullReq, result.toString()))
					.addListener(ChannelFutureListener.CLOSE);
		} catch (Throwable e) {
			Log.error("处理http请求异常", e);
			ctx.writeAndFlush(HttpMsgUtil.buildHttpMsg(fullReq, "{\"c\":0,\"error\":\"inner error\"}"))
					.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * <pre>
	 * 处理收到的json数据
	 * 格式：{
	 * 	 code:
	 * }
	 * </pre>
	 *
	 * @param ctx
	 * @param jsonStr
	 */
	protected Object handleMsgReceived(ChannelHandlerContext ctx, JSONObject jsonObj) {
		GameServer.stopServer();
		return "no sub class";
	}

}
