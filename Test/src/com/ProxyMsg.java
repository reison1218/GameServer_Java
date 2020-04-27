package com;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;


public class ProxyMsg {
    public static int MIN_HEADER_LEN = 16;
    private int len;//存放整个包的长度
    private byte sendType;//发送类型, 上行/下行
    private byte option;//包体类型b0字符串,b1-proto
    private byte type;//请求服务类型
    private int serverID;//ServerID
    private byte cmd;//命令ID
    private int seq;//请求序号
    private byte[] data;//协议体数据

    public int getServerID() {
        return serverID;
    }

    public byte getSendType() {
        return sendType;
    }

    public void setSendType(byte sendType) {
        this.sendType = sendType;
    }

    public byte getOption() {
        return option;
    }

    public void setOption(byte option) {
        this.option = option;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void decode(ByteBuf buffer) {
        this.len = buffer.readInt();
        this.sendType = buffer.readByte();
        this.option = buffer.readByte();
        this.type = buffer.readByte();
        this.serverID = buffer.readInt();
        this.cmd = buffer.readByte();
        this.seq = buffer.readInt();
        this.data = new byte[len - MIN_HEADER_LEN];
        buffer.readBytes(data);
    }

    /**
     * 编码
     */
    public void encode(ByteBuf buffer) {
        this.getLen();
        buffer.writeInt(len);
        buffer.writeByte(this.sendType);
        buffer.writeByte(this.option);
        buffer.writeByte(this.type);
        buffer.writeInt(this.serverID);
        buffer.writeByte(this.cmd);
        buffer.writeInt(this.seq);
        if (data != null)
            buffer.writeBytes(data);
    }

    /**
     * 将包编码字节流
     */
    public byte[] getEncodeData() {
        this.getLen();
        ByteBuffer bb = ByteBuffer.allocate(len);
        bb.putInt(len);
        bb.put(this.sendType);
        bb.put(this.option);
        bb.put(this.type);
        bb.putInt(this.serverID);
        bb.put(this.cmd);
        bb.putInt(this.seq);
        if (data != null)
            bb.put(data);
        return bb.array();
    }

    public int getLen() {
        len = MIN_HEADER_LEN;
        if (data != null)
            len += data.length;
        return len;
    }
}
