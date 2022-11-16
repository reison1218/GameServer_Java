/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.packet;

import com.google.protobuf.InvalidProtocolBufferException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import game.protobuf.GPMsg.CommandType;
import game.protobuf.GPMsg.TMSG_HEADER;
import io.netty.buffer.ByteBuf;

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
    private static final Logger logger = LoggerFactory.getLogger(Packet.class);
    /**
     * 包体的消息头
     */
    private byte[] header;

    /**
     * 包体的消息体
     */
    private byte[] body;

    private TMSG_HEADER headerProto;


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
    public static Packet read(ByteBuf msg) {
        try {
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
        } catch (Exception e) {
            logger.error("Packet 解包异常", e);
        }
        return null;
    }

    public CommandType getCommand() {
        if (this.headerProto == null) {
            return CommandType.CommandType_Unknow;
        }
        return this.headerProto.getCommand();
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setHeader(byte[] header) throws InvalidProtocolBufferException {
        this.header = header;
        TMSG_HEADER.Builder builder = TMSG_HEADER.newBuilder();
        builder.mergeFrom(this.header);
        this.headerProto = builder.build();
    }

    public byte[] getHeader() {
        return header;
    }

    public void release() {
        this.body = null;
        this.headerProto = null;
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
