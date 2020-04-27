package com.base.netty.handler;

import com.base.netty.packet.Packet;
import com.utils.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

/**
 * gate handler入口
 * 
 * @author reison
 *
 */
public abstract class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	// websocket服务的 uri
	private static final String WEB_SOCKET_UPGRADE = "Upgrade";
	private static final String WEB_SOCKET = "websocket";

	private WebSocketServerHandshaker handshaker;

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		ChannelFuture future = ctx.channel().close();
		if (future.isDone()) {
			Log.error("WebSocketServerHandler has error! close the channel,message is:" + cause.getMessage());
		}
	}

	/**
	 * http请求
	 *
	 * @param ctx
	 * @param req
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// Handle a bad request.
		if (!req.decoderResult().isSuccess()) {
			// sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
			// BAD_REQUEST));
			return;
		}

		// 检查升级websocket
		checkUpgrade(ctx, req);
	}

	/**
	 * 检查升级websocket
	 * 
	 * @param ctx
	 * @param req
	 */
	private void checkUpgrade(ChannelHandlerContext ctx, FullHttpRequest req) {
		String connection = req.headers().get(HttpHeaderNames.CONNECTION);
		String upgrade = req.headers().get(HttpHeaderNames.UPGRADE);
		// 校验消息头,是否要升级为websocket
		if (WEB_SOCKET_UPGRADE.equalsIgnoreCase(connection) && WEB_SOCKET.equalsIgnoreCase(upgrade)) {

			// webSocket升级握手
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketURL(req),
					null, true);
			handshaker = wsFactory.newHandshaker(req);

			// 校验是否为null
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				return;
			}

			ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);
			// 握手成功之后,业务逻辑
			if (channelFuture.isSuccess()) {
				Log.info("client has upgrade websocket,ip:" + ctx.channel().remoteAddress());
			}
		}
	}

	private String getWebSocketURL(HttpRequest req) {
		return "ws://" + req.headers().get("Host") + req.uri();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	/**
	 * 处理websocket
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if (msg instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
			return;
		}
		if (msg instanceof PingWebSocketFrame) { // pingpong测试
			ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
			return;
		}
		if (msg instanceof TextWebSocketFrame) {
			handleTextFrame(ctx, (TextWebSocketFrame) msg);
		} else if (msg instanceof BinaryWebSocketFrame) {
			handleBinaryFrame(ctx, (BinaryWebSocketFrame) msg);
		}
	}

	/**
	 * 处理文本
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
		// TODO
	}

	/**
	 * 处理二进制数据
	 * 
	 * @param ctx
	 * @param msg
	 */
	private void handleBinaryFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {
		try {
			ByteBuf bb = msg.content();
			// 长度
			int length = bb.readInt();
			// 命令
			int cmd = bb.readInt();
			// 空读8个byte,占位字节?
			bb.readInt();
			bb.readInt();
			// 创建一个byte数组，去掉之前读掉的16个byte
			byte[] bytes = new byte[length - 16];
			// 剩下的读到bytes里面去
			bb.readBytes(bytes);
			Packet packet = new Packet((short) cmd, 0, bytes);
			// 消息分发
			handleMsgReceived(ctx, packet);
		} catch (Exception e) {
			Log.error("handleBinaryFrame has error!,message:" + e.getMessage());
		}

	}

	/**
	 * <pre>
	 * 处理收到的包
	 * </pre>
	 *
	 * @param ctx
	 * @param jsonObj
	 */
	protected abstract void handleMsgReceived(ChannelHandlerContext ctx, Packet packet);

}
