/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.tcp;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;

/**
 * <pre>
 * Netty客户端基类
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public class SocketClient extends BaseNettyClient {

	public SocketClient(String name, String ip, int port, int workThreadCount,
			ChannelInitializer<SocketChannel> webSocketServerInitializer) {
		super(name, ip, port, workThreadCount, webSocketServerInitializer);
	}

	/**
	 * @param b
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void initParam(Bootstrap b) {
		// socket关闭时，等待5秒，让数据发送完毕
		b.option(ChannelOption.SO_LINGER, 5);
		// 不管数据包大小，不组合包，直接发送
		b.option(ChannelOption.TCP_NODELAY, true);
		// 若无数据传输时，2小时一次探测包，判断连接是否仍然正常
		b.option(ChannelOption.SO_KEEPALIVE, true);
		// 接收区缓存大小(32K=短整型最大值)
		b.option(ChannelOption.SO_RCVBUF, 8 * 1024);
		// 发送区缓存大小(32K)
		b.option(ChannelOption.SO_SNDBUF, 16 * 1024);
		// 默认使用内存池分配ByteBuf
		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// 启用接收区缓存自动调节大小策略
		b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 64));

		b.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 256, 1024 * 512));
	}

}
