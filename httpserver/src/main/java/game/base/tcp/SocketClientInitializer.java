package game.base.tcp;

import game.handler.tcp.SocketClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;

/**
 * socket客户端初始器
 * @author reison
 *
 */
public class SocketClientInitializer extends ChannelInitializer<SocketChannel> {

	private SocketClientHandler handler;

	public SocketClientInitializer(SocketClientHandler handler) {
		this.handler = handler;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		// 使用lz4快速压缩
//		 pipeline.addLast("decoder", new Lz4FrameDecoder());
//		 pipeline.addLast("encoder", new Lz4FrameEncoder());
		// 第1、2位表示整包的长度,3、4位表示包描述信息长度,后续为描述信息字节,以及逻辑数据字节
//		pipeline.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 2, 2, -4, 4, false));
		pipeline.addLast(handler.getClass().newInstance());
	}
}
