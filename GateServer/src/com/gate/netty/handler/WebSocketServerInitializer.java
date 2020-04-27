package com.gate.netty.handler;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * websocket初始器
 * 
 * @author reison
 *
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipe = ch.pipeline();

		// http编解码handler
		pipe.addLast(new HttpServerCodec());
		// 处理大型文件（暂时不需要）
		// pipe.addLast(new ChunkedWriteHandler());
		// http数据包聚合handler
		pipe.addLast(new HttpObjectAggregator(65535));
		// websocket压缩handler
		pipe.addLast(new WebSocketServerCompressionHandler());
		// 心跳检测handler
		pipe.addLast(new IdleStateHandler(0, 0, 25, TimeUnit.SECONDS));
		// 网络事件处理handler
		pipe.addLast(new GateWebSocketServerHandler());
	}

}
