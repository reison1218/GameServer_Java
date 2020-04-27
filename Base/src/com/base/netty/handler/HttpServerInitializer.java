/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <pre>
 * http服务器初始化
 * </pre>
 * 
 * @author reison
 * @time 2017年4月12日
 */
public final class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

	private HttpServerHandler handler;
	private final SslContext sslCtx;

	public HttpServerInitializer(SslContext sslCtx, HttpServerHandler handler) {
		this.handler = handler;
		this.sslCtx = sslCtx;
	}

	/**
	 * @param ch
	 * @throws Exception
	 * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
	 */
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpResponseEncoder());
		pipeline.addLast(new HttpRequestDecoder());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new ChunkedWriteHandler());
		// pipeline.addLast(new CorsHandler(corsConfig));
		pipeline.addLast(handler.getClass().newInstance());
	}

}
