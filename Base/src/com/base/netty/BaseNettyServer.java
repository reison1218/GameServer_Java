/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.utils.Log;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * <pre>
 * Netty服务器基类
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public abstract class BaseNettyServer {

	protected Channel ch;
	protected boolean start;
	protected EventLoopGroup bossGroup;
	protected EventLoopGroup workerGroup;
	private final AtomicInteger threadIdx = new AtomicInteger();
	public final static String THREAD_NAME_PREFIX = "NettySvr-";

	/**
	 * <pre>
	 * 设置参数
	 * </pre>
	 */
	protected abstract void initParam(ServerBootstrap b);

	public BaseNettyServer(int port, int workThreadCount, ChannelInitializer<SocketChannel> serverInitializer) {
		init(port, workThreadCount, serverInitializer);
	}

	/**
	 * 初始化网络层（netty）
	 * 
	 * @param port
	 * @param workThreadCount
	 * @param serverInitializer
	 */
	private final void init(int port, int workThreadCount, ChannelInitializer<SocketChannel> serverInitializer) {
		final String name = this.getClass().getSimpleName();

		bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, name + "-BossThread");
			}
		});
		workerGroup = new NioEventLoopGroup(workThreadCount, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, THREAD_NAME_PREFIX + name + "-Worker-" + threadIdx.incrementAndGet());
			}
		});
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(serverInitializer);
			initParam(b);
			ch = b.bind(port).sync().channel();
			Log.info(name + "监听端口：" + port);
			start = true;
		} catch (Throwable e) {
			Log.error("Netty启动异常,端口：" + port, e);
		}
	}

	/**
	 * <pre>
	 * 停止服务器
	 * </pre>
	 */
	public final void stop() {
		try {
			ch.close().syncUninterruptibly();
			ch.closeFuture().syncUninterruptibly();
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		} catch (Throwable e) {
			Log.error("NettyServer停止异常 : ", e);
		}
	}

	public final boolean isStart() {
		return start;
	}

}
