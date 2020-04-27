/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.packet;

import java.util.Arrays;

import com.base.netty.util.MessageUtil;
import com.utils.PStuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * <pre>
 * 网关转发包
 * ！数据体无需解包
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public final class Packet {

	/** 包描述信息 */
	private PacketDesc desc;

	/** 数据体字节 */
	private byte[] body;

	/** ws帧数据 */
	private BinaryWebSocketFrame frame;

	public Packet() {
		// 给默认数据，不然其他地方会空指针
		body = new byte[1];
	}

	public Packet(int code, int userId, byte[] body) {
		this.desc = new PacketDesc(code, userId, true);
		this.body = body;
	}

	/**
	 * <pre>
	 * 从ByteBuf解包
	 * </pre>
	 *
	 * @param msg
	 * @return Packet
	 */
	public final static Packet read(ByteBuf msg) {
		Packet packet = new Packet();
		// 包描述信息长度
		int descLen = msg.readShort();
		byte[] descBs = new byte[descLen];
		msg.readBytes(descBs);
		PacketDesc desc = new PacketDesc();
		packet.setDesc(desc);
		// 反序列化描述信息
		PStuffUtil.deserialize(PacketDesc.schema, descBs, desc);
		// 数据体长度
		int bodyLen = msg.readableBytes();
		byte[] bs = new byte[bodyLen];
		msg.readBytes(bs);
		packet.setBody(bs);
		return packet;
	}

	public final PacketDesc getDesc() {
		return desc;
	}

	public final void setDesc(PacketDesc desc) {
		this.desc = desc;
	}

	public final byte[] getBody() {
		return body;
	}

	/**
	 * <pre>
	 * 构建ws帧
	 * </pre>
	 *
	 * @param ch
	 * @return
	 */
	public final BinaryWebSocketFrame buildFrame(Channel ch) {
		if (frame != null) {
			return frame;
		}
		frame = MessageUtil.buildBinaryFrame(desc.getCode(), body, ch);
		return frame;
	}

	public final void setBody(byte[] body) {
		this.body = body;
	}

	public final void release() {
		this.body = null;
		this.frame = null;
		this.desc.release();
		this.desc = null;
	}

	public final int getCode() {
		if (this.desc == null)
			return 0;
		return this.desc.getCode();
	}

	public final int getUserId() {
		if (this.desc == null)
			return 0;
		return this.desc.getUserId();
	}

	public void setUserId(int userId) {
		if (this.desc == null)
			return;
		this.desc.setUserId(userId);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Packet [desc=" + desc + ", body=" + Arrays.toString(body) + "]";
	}

}
