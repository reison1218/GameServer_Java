/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.base.netty.packet.Packet;
import com.base.netty.packet.PacketDesc;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.PStuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

public final class MessageUtil {

	/** 防粘包\n分隔符 */
	public final static byte[] SEG_BYTES = "\n".getBytes();

	/**
	 * <pre>
	 * 构建发送到客户端的数据包
	 * </pre>
	 *
	 * @param code
	 * @param isClient
	 * @param body
	 * @param channel
	 * @return
	 */
	public final static ByteBuf buildClientBuf(int code, int userId, Object body, Channel channel) {
		return buildBufForClient(code, body, channel, userId, true, false, null);
	}

	/**
	 * <pre>
	 * 构建发送到服务器的数据包
	 * </pre>
	 *
	 * @param code
	 * @param body
	 * @param fromClient
	 * @param rpcReqId   rpc请求Id
	 * @param channel
	 * @return
	 */
	public final static ByteBuf buildServerBuf(int code, Object body, Channel channel) {
		return buildBuf(code, body, channel, 0, false, false, null);
	}

	/**
	 * <pre>
	 * 构建发送到服务器的数据包
	 * </pre>
	 *
	 * @param code
	 * @param body
	 * @param fromClient
	 * @param rpcReqId   rpc请求Id
	 * @param channel
	 * @return
	 */
	public final static ByteBuf buildServerBuf(int code, int userId, Object body, Channel channel) {
		return buildBuf(code, body, channel, userId, false, false, null);
	}

	/**
	 * <pre>
	 * 构建广播部分人的数据包
	 * </pre>
	 *
	 * @param code      协议号
	 * @param body      需广播的数据包
	 * @param channel
	 * @param broadUids 需要广播的用户Id(传空为所有人, 不能和其他引用关联, 因为会被clear)
	 * @return
	 */
	public final static ByteBuf buildBroadAllBuf(short code, Object body, Channel channel,
			Collection<Integer> broadUids) {
		return buildBuf(code, body, channel, 0, true, true, broadUids);
	}

	/**
	 * <pre>
	 * 构建Buf数据包
	 * 用于服务器和网关之间通讯
	 * </pre>
	 *
	 * @param code
	 * @param body      数据包内容
	 * @param channel   连接
	 * @param userId
	 * @param isClient  是否为客户端包
	 * @param isBroad   是否为广播包
	 * @param broadUids 需要广播的用户Id(传空为所有人, 不能和其他引用关联, 因为会被clear)
	 * @return
	 */
	public final static ByteBuf buildBuf(int code, Object body, Channel channel, int userId, boolean isClient,
			boolean isBroad, Collection<Integer> broadUids) {
		if (body == null) {
			body = new HashMap<>();
		}
		byte[] bs = JsonUtil.binaryify(body);
		PacketDesc desc = new PacketDesc(code, userId, isClient);
		if (isBroad) {
			desc.setBroad(isBroad);
			if (broadUids != null && !broadUids.isEmpty()) {
				desc.setBroadUids(broadUids);
			}
		}
		byte[] descBs = PStuffUtil.serialize(PacketDesc.schema, desc);
		int len = 2 + 2 + descBs.length + bs.length;
		// 整个包长度(用于处理粘包半包)
		if (len > Short.MAX_VALUE) {
			Log.error("数据包len" + len + "超过" + Short.MAX_VALUE + ",发送失败,userId:" + userId + ",code:" + code + "\npacket:"
					+ JsonUtil.stringify(body));
			return null;
		}
		ByteBuf buf = null;
		if (channel != null) {
			buf = channel.alloc().buffer(len);
		} else {
			buf = Unpooled.buffer(len);
		}
		buf.writeShort(len);
		// 描述信息长度
		buf.writeShort(descBs.length);
		// 描述信息字节
		buf.writeBytes(descBs);
		// 逻辑数据字节
		buf.writeBytes(bs);
		// 释放内存
		bs = null;
		descBs = null;
		return buf;
	}

	/**
	 * <pre>
	 * 构建Buf数据包
	 * 用于服务器和网关之间通讯
	 * </pre>
	 *
	 * @param code
	 * @param body      数据包内容
	 * @param channel   连接
	 * @param userId
	 * @param isClient  是否为客户端包
	 * @param isBroad   是否为广播包
	 * @param broadUids 需要广播的用户Id(传空为所有人, 不能和其他引用关联, 因为会被clear)
	 * @return
	 */
	public final static ByteBuf buildBufForClient(int code, Object body, Channel channel, int userId, boolean isClient,
			boolean isBroad, Collection<Integer> broadUids) {
		if (body == null)
			return null;
		byte[] bs = (byte[]) body;
		PacketDesc desc = new PacketDesc(code, userId, isClient);
		if (isBroad) {
			desc.setBroad(isBroad);
			if (broadUids != null && !broadUids.isEmpty()) {
				desc.setBroadUids(broadUids);
			}
		}
		byte[] descBs = PStuffUtil.serialize(PacketDesc.schema, desc);
		int len = 2 + 2 + descBs.length + bs.length;
		// 整个包长度(用于处理粘包半包)
		if (len > Short.MAX_VALUE) {
			Log.error("数据包len" + len + "超过" + Short.MAX_VALUE + ",发送失败,userId:" + userId + ",code:" + code + "\npacket:"
					+ JsonUtil.stringify(body));
			return null;
		}
		ByteBuf buf = null;
		if (channel != null) {
			buf = channel.alloc().buffer(len);
		} else {
			buf = Unpooled.buffer(len);
		}
		buf.writeShort(len);
		// 描述信息长度
		buf.writeShort(descBs.length);
		// 描述信息字节
		buf.writeBytes(descBs);
		// 逻辑数据字节
		buf.writeBytes(bs);
		// 释放内存
		bs = null;
		descBs = null;
		return buf;
	}

	/**
	 * <pre>
	 * 通过Packet构建数据包
	 * </pre>
	 *
	 * @param channel
	 * @param packet
	 * @return
	 */
	public final static ByteBuf buildBufByPacket(Channel channel, Packet packet) {
		byte[] descBs = PStuffUtil.serialize(PacketDesc.schema, packet.getDesc());
		byte[] bs = packet.getBody();
		int len = 2 + 2 + descBs.length + bs.length;
		// 整个包长度(用于处理粘包半包)
		if (len > Short.MAX_VALUE) {
			Log.error("数据包len" + len + "超过" + Short.MAX_VALUE + ",发送失败,userId:" + packet.getDesc().getUserId()
					+ ",code:" + packet.getDesc().getCode() + "\npacket:"
					+ JsonUtil.stringify(JsonUtil.parse(packet.getBody())));
			return null;
		}
		ByteBuf buf = null;
		if (channel != null) {
			buf = channel.alloc().buffer(len);
		} else {
			buf = Unpooled.buffer(len);
		}
		buf.writeShort(len);
		// 描述信息长度
		buf.writeShort(descBs.length);
		// 描述信息字节
		buf.writeBytes(descBs);
		// 逻辑数据字节
		buf.writeBytes(bs);
		// 释放内存
		descBs = null;
		packet.release();
		packet = null;
		return buf;
	}

	/**
	 * <pre>
	 * 构建ws二进制数据包
	 * </pre>
	 * 
	 * int:len+int:cmd+[int+int]+protobuf
	 * 
	 * @param code
	 * @param body
	 * @return
	 */
	public final static BinaryWebSocketFrame buildBinaryFrame(int code, byte[] body, Channel clientChannel) {
		int len = body.length + 16;
		// 数据长度
		if (len > Short.MAX_VALUE) {
			Log.error("数据包len" + body.length + "超过" + Short.MAX_VALUE + ",发送失败,code:" + code + "\npacket:"
					+ JsonUtil.stringify(JsonUtil.parse(body)));
			return null;
		}
		ByteBuf buf = null;
		if (clientChannel != null) {
			buf = clientChannel.alloc().buffer(len);
		} else {
			buf = Unpooled.buffer(len);
		}
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buf);
		// 协议号
		buf.writeInt(len);
		buf.writeInt(code);
		buf.writeInt(0);
		buf.writeInt(0);
		// 数据字节
		buf.writeBytes(body);
		// 释放内存
		body = null;
		return frame;
	}

	/**
	 * <pre>
	 * 构建空数据包
	 * </pre>
	 *
	 * @param code
	 * @return
	 */
	public final static BinaryWebSocketFrame buildEmpty(int code, Channel clientChannel) {
		ByteBuf buf = null;
		if (clientChannel != null) {
			buf = clientChannel.alloc().buffer(4);
		} else {
			buf = Unpooled.buffer(4);
		}
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buf);
		// 协议号
		buf.writeShort(code);
		// 数据长度0
		buf.writeShort(0);
		return frame;
	}

	/**
	 * <pre>
	 * 发送服务器间包
	 * ！不等待发包完成
	 * </pre>
	 *
	 * @param buf
	 * @param ch
	 */
	public final static void writeChannel(ByteBuf buf, Channel ch) {
		if (ch == null || !ch.isActive()) {
			if (buf != null) {
				ReferenceCountUtil.release(buf);
			}
			return;
		}
		checkWritable(ch);
		ch.writeAndFlush(buf);
	}

	/**
	 * <pre>
	 * 发送服务器间包
	 * </pre>
	 *
	 * @param buf       数据包
	 * @param chs       需要发送的连接集合
	 * @param isDiscard 在不可写的时候是否可忽略不发送
	 */
	public final static void writeChannel(ByteBuf buf, Collection<Channel> chs, boolean isDiscard) {
		int size = chs.size();
		ByteBuf retain = buf;
		if (size > 1) {
			retain = buf.retain(size - 1);
		}
		if (retain == null) {
			return;
		}
		for (Channel ch : chs) {
			// 堆外内存泄漏测试
			// if (RandomUtil.rand100() <= 70) {
			// continue;
			// }
			if (ch == null || !ch.isActive() || (!ch.isWritable() && isDiscard)) {
				ReferenceCountUtil.release(retain);
				continue;
			}
			checkWritable(ch);
			ch.writeAndFlush(retain.duplicate());
		}
	}

	/**
	 * *
	 * 
	 * <pre>
	 * 发送客户端包
	 * </pre>
	 *
	 * @param buf       数据包
	 * @param chs       需要发送的连接集合
	 * @param isDiscard 在不可写的时候是否可忽略不发送
	 */
	public final static void writeChannel(BinaryWebSocketFrame buf, Collection<Channel> chs, boolean isDiscard) {
		int size = chs.size();
		BinaryWebSocketFrame retain = buf;
		if (size > 1) {
			retain = buf.retain(size - 1);
		}
		for (Channel ch : chs) {
			if (ch == null || !ch.isActive() || (!ch.isWritable() && isDiscard)) {
				ReferenceCountUtil.release(retain);
				continue;
			}
			checkWritable(ch);
			ch.writeAndFlush(retain.duplicate());
		}
	}

	/** 发包数统计 */
	private final static AtomicLong counter = new AtomicLong();

	/**
	 * <pre>
	 * 等待连接可写
	 * </pre>
	 *
	 * @param ch
	 */
	public final static void checkWritable(Channel ch) {
		int count = 0;
		while (ch != null && !ch.isWritable() && ++count < 50) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				Log.error("", e);
			}
		}
		if (count > 10) {
			System.err.println("LogCheckWritable,cost:" + (count * 2) + "ms");
		}
		if (counter.incrementAndGet() % 1000 == 0) {
			// TODO
		}
	}

}
