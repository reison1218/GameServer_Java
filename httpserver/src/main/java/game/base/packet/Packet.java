/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.packet;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * <pre>
 * 数据包
 * 包头不用存，只用存包体里面的消息头和消息体
 * </pre>
 *
 * @author reison
 * @time 2019年7月26日
 */
public final class Packet {

    /**
     * 包体的消息头
     */
    private byte[] header;

    /**
     * 包体的消息体
     */
    private byte[] body;

    /**
     * ws帧数据
     */
    private BinaryWebSocketFrame frame;

    public Packet() {
        // 给默认数据，不然其他地方会空指针
        body = new byte[1];
    }

    /**
     * <pre>
     * 从ByteBuf解包
     * </pre>
     *
     * @return Packet
     */
    public final static Packet read(ByteBuf msg) {
        //[16, 0, 8, 0, 8, -35, 16, 16, -35, 16, 24, 2, 8, -35, 16, 16, -114, 89]
        Packet packet = new Packet();
        //包体整体长度
        int bodyLen = msg.readUnsignedShortLE();
        //包体消息头长度
        int bodyHeaderLen = msg.readUnsignedShortLE();
        //包体消息体长度
        int bodyBodyLen = bodyLen - 2 - bodyHeaderLen;

        //包体消息头
        byte[] bodyHeader = new byte[bodyHeaderLen];
        msg.readBytes(bodyHeader);
        packet.setHeader(bodyHeader);

        //包体消息体
        byte[] bodyBody = new byte[bodyBodyLen];
        msg.readBytes(bodyBody);
        packet.setBody(bodyBody);
        return packet;
    }

    public final byte[] getBody() {
        return body;
    }

    public final void setBody(byte[] body) {
        this.body = body;
    }

    public final void setHeader(byte[] header) {
        this.header = header;
    }

    public final byte[] getHeader() {
        return header;
    }

    public final void release() {
        this.body = null;
        this.frame = null;
        this.header = null;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Packet [header=" + Arrays.toString(header) + ", body=" + Arrays.toString(body) + "]";
    }

}
