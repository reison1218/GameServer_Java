/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.socketserver;


import com.base.netty.BaseNettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;

/**
 * <pre>
 * SocketServer基类
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public class SocketServer extends BaseNettyServer {

	public SocketServer(int port, int workThreadCount, ChannelInitializer<SocketChannel> serverInitializer) {
		super(port, workThreadCount, serverInitializer);
	}

	/**
	 * @param b
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void initParam(ServerBootstrap b) {
		// socket关闭时，等待5秒，让数据发送完毕
		b.option(ChannelOption.SO_LINGER, 5);
		// bossGroup同一时间只能处理一个客户端连接，多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
		b.option(ChannelOption.SO_BACKLOG, 512);
		// 不管数据包大小，不组合包，直接发送
		b.childOption(ChannelOption.TCP_NODELAY, true);
		// 若无数据传输时，2小时一次探测包，判断连接是否仍然正常
		b.childOption(ChannelOption.SO_KEEPALIVE, true);
		// 接收区缓存大小(32K=短整型最大值)
		b.option(ChannelOption.SO_RCVBUF, 16 * 1024);
		// 发送区缓存大小(32K)
		b.option(ChannelOption.SO_SNDBUF, 16 * 1024);
		// 接收区缓存大小(32K=短整型最大值)
		b.childOption(ChannelOption.SO_RCVBUF, 16 * 1024);
		// 发送区缓存大小(32K)
		b.childOption(ChannelOption.SO_SNDBUF, 16 * 1024);
		// 默认使用内存池分配ByteBuf
		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// 启用接收区缓存自动调节大小策略
		b.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

		b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 512, 1024 * 1024));
	}

}
