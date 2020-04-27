package com.game.netty.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class GameSocketServerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		// 第12位表示整包的长度,34位表示包描述信息长度,后续为描述信息字节,以及逻辑数据字节
		pipeline.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, -2, 2, false));
		pipeline.addLast(new GameSocketServerHandler());
	}
}
