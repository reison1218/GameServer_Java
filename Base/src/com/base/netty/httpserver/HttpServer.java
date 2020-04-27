package com.base.netty.httpserver;

import com.base.netty.BaseNettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;

public class HttpServer extends BaseNettyServer {

	public HttpServer(int port, int workThreadCount, ChannelInitializer<SocketChannel> serverInitializer) {
		super(port, workThreadCount, serverInitializer);
	}

	/**
	 * @param b
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void initParam(ServerBootstrap b) {
		// 不管数据包大小，不组合包，直接发送
		b.childOption(ChannelOption.TCP_NODELAY, true);
		// 默认使用内存池分配ByteBuf
		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// 启用接收区缓存自动调节大小策略
		b.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
	}
}
