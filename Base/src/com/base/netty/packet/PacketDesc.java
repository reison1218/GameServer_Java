/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.netty.packet;

import java.util.Collection;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * <pre>
 * 网关转发包描述信息
 * ！避免在网关要解整包
 * </pre>
 * 
 * @author reison
 * @time 2019年7月26日
 */
public class PacketDesc {

	/** 序列化描述模板 */
	public static Schema<PacketDesc> schema = null;

	/** 协议号 */
	private int cmd;

	/** userId */
	private int userId;

	/** 是否为客户端发出或者发给客户端的包 */
	private boolean isClient;

	/** 该数据包是否需要广播 */
	private boolean isBroad;

	/** 该数据包需要广播的用户Ids */
	private Collection<Integer> broadUids;

	static {
		schema = RuntimeSchema.getSchema(PacketDesc.class);
	}

	public PacketDesc() {
	}

	public PacketDesc(int cmd, int userId, boolean isClient) {
		this.cmd = cmd;
		this.userId = userId;
		this.isClient = isClient;
	}

	public int getUserId() {
		return userId;
	}

	public int getCode() {
		return cmd;
	}

	public final boolean isClient() {
		return isClient;
	}

	public final boolean isBroad() {
		return isBroad;
	}

	public final void setBroad(boolean isBroad) {
		this.isBroad = isBroad;
	}

	public final Collection<Integer> getBroadUids() {
		return broadUids;
	}

	public final void setBroadUids(Collection<Integer> broadUids) {
		this.broadUids = broadUids;
	}

	public final void release() {
		if (this.broadUids != null) {
			this.broadUids.clear();
			this.broadUids = null;
		}
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PacketDesc [cmd=" + cmd + ", userId=" + userId + ", isClient=" + isClient + ", isBroad=" + isBroad + ", broadUids=" + broadUids + "]";
	}

}
