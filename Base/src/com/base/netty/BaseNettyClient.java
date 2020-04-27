/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.utils.Log;
import com.utils.RandomUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * <pre>
 * Netty客户端基类
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public abstract class BaseNettyClient {

	private int port;
	private String ip;
	private Bootstrap b;
	private String clientName;
	protected EventLoopGroup workerGroup;
	protected final List<Channel> chs = new ArrayList<>();
	private final AtomicInteger threadIdx = new AtomicInteger();

	private int clientCount;

	public BaseNettyClient(final String name, String ip, int port, int workThreadCount,
			ChannelInitializer<SocketChannel> webSocketServerInitializer) {

		this.workerGroup = new NioEventLoopGroup(workThreadCount, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, name + "-ClientThread-" + threadIdx.incrementAndGet());
			}
		});
		clientCount = workThreadCount;
		try {
			this.ip = ip;
			this.port = port;
			this.clientName = name;
			this.b = new Bootstrap();
			b.group(workerGroup).channel(NioSocketChannel.class).handler(webSocketServerInitializer);
			initParam(b);
			for (int i = 0; i < workThreadCount; i++) {
				chs.add(b.connect(this.ip, this.port).sync().channel());
			}
			Log.info("连接" + name + "成功，ip：" + ip + ",port:" + port);
		} catch (Throwable e) {
			Log.error(clientName + "客户端启动异常", e);
		}
	}

	/**
	 * <pre>
	 * 重连某个连接
	 * </pre>
	 *
	 * @param randIdx
	 * @return
	 */
	public final Channel reconnect(int randIdx) {
		if (randIdx < 0 || randIdx > chs.size() - 1) {
			return null;
		}
		Channel ch = chs.get(randIdx);
		try {
			if (ch != null) {
				ch.close();
			}
		} catch (Throwable e) {
			Log.error("关闭原连接异常", e);
		}
		try {
			ch = b.connect(this.ip, this.port).sync().channel();
			chs.set(randIdx, ch);
		} catch (Throwable e) {
			Log.error("网关重连" + clientName + "失败,请检查网络或相应进程,ip:" + this.ip + ",port:" + this.port, e);
		}
		if (ch != null && ch.isActive()) {
			Log.info("网关重连" + clientName + "成功：" + ch);
		}
		return ch;
	}

	/**
	 * <pre>
	 * 重连
	 * </pre>
	 *
	 * @param randIdx
	 * @return
	 */
	public final boolean reconnect() {

		try {
			if (chs != null && !chs.isEmpty()) {
				// 如果有活的，不用重新连
				if (chs.get(0).isActive())
					return true;

				for (Channel ch : chs) {
					if (ch == null) {
						continue;
					}
					ch.close();
				}
			}
			// 清掉原来的
			chs.clear();
		} catch (Throwable e) {
			Log.error("关闭原连接异常", e);
			return false;
		}
		try {
			// 重新连
			for (int i = 0; i < clientCount; i++) {
				chs.add(b.connect(this.ip, this.port).sync().channel());
			}

		} catch (Throwable e) {
			Log.error("网关重连" + clientName + "失败,请检查网络或相应进程,ip:" + this.ip + ",port:" + this.port, e);
			return false;
		}
		if (chs != null && !chs.isEmpty()) {
			Log.info("网关重连" + clientName + "成功!");
		}
		return true;
	}

	/**
	 * <pre>
	 * 获取连接集合
	 * </pre>
	 *
	 * @return
	 */
	public final List<Channel> getChs() {
		return chs;
	}

	/**
	 * <pre>
	 * 随机获取连接
	 * </pre>
	 *
	 * @return
	 */
	public final Channel selChannel() {
		final List<Channel> chs = this.chs;
		if (chs == null || chs.isEmpty()) {
			return null;
		}
		int randIdx = RandomUtil.rand(chs.size());
		Channel conn = chs.get(randIdx);
		// 检测连接
		if (!isActive(conn)) {
			// 重连服务器
			conn = reconnect(randIdx);
			if (!isActive(conn)) {
				return null;
			}
		}
		return conn;
	}

	/**
	 * <pre>
	 * 连接是否有效
	 * </pre>
	 *
	 * @param channel
	 * @return
	 */
	private static final boolean isActive(Channel channel) {
		return channel != null && channel.isOpen() && channel.isActive();
	}

	/**
	 * <pre>
	 * 设置客户端参数
	 * </pre>
	 */
	protected abstract void initParam(Bootstrap b);

	/**
	 * <pre>
	 * 停止客户端
	 * </pre>
	 */
	public final void stop() {
		try {
			for (Channel channel : chs) {
				channel.close();
				channel.closeFuture().syncUninterruptibly();
			}
			workerGroup.shutdownGracefully();
		} catch (Throwable e) {
			Log.error(clientName + "客户端停止异常 : ", e);
		}
	}

	/**
	 * <pre>
	 * 创建客户端
	 * </pre>
	 *
	 * @param clazz
	 * @param port
	 * @param workThreadCount
	 * @param serverInitializer
	 * @return
	 */
	public final static BaseNettyClient createInstance(Class<?> clazz, String name, String host, int port,
			int workThreadCount, ChannelInitializer<SocketChannel> serverInitializer) {
		try {
			Constructor<?> constructor = clazz.getConstructor(String.class, String.class, int.class, int.class,
					ChannelInitializer.class);
			if (constructor == null) {
				Log.error("客户端构造方法参数错误：" + clazz.getName());
				return null;
			}
			constructor.setAccessible(true);
			return (BaseNettyClient) constructor.newInstance(name, host, port, workThreadCount, serverInitializer);
		} catch (Throwable e) {
			Log.error("", e);
		}
		return null;
	}

}
