/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.utils;

import java.util.concurrent.atomic.AtomicLong;

import game.base.packet.Packet;
import game.protobuf.GPMsg.CommandType;
import game.protobuf.GPMsg.TMSG_HEADER;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

public final class MessageUtil {

    /**
     * 防粘包\n分隔符
     */
    public final static byte[] SEG_BYTES = "\n".getBytes();

    /**
     * <pre>
     * 构建Buf数据包
     * 用于服务器和网关之间通讯
     * </pre>
     */
    public final static ByteBuf buildBuf(Packet packet, Channel channel) {

        int len = 2 + packet.getHeader().length + packet.getBody().length;
        // 整个包长度(用于处理粘包半包)
        if (len > Short.MAX_VALUE) {
            Log.error("数据包len" + len + "超过" + Short.MAX_VALUE + ",发送失败,len:" + len + "\npacket:" + JsonUtil.stringify(packet.getBody()));
            return null;
        }
        ByteBuf buf;
        if (channel != null) {
            buf = channel.alloc().buffer(len);
        } else {
            buf = Unpooled.buffer(len);
        }
        buf.writeShort(len);
        // 描述信息长度
        buf.writeShort(packet.getHeader().length);
        // 描述信息字节
        buf.writeBytes(packet.getHeader());
        // 逻辑数据字节
        buf.writeBytes(packet.getBody());
        return buf;
    }

    public static void sendMessage(CommandType cmdType,byte[] body,Channel channel){
        //拼装消息头
        TMSG_HEADER.Builder headerBuilder = TMSG_HEADER.newBuilder();
        headerBuilder.setGameID(2141);
        headerBuilder.setCommand(cmdType);
        headerBuilder.setWorldID(2141);
        ByteBuf bb = buildBuf(headerBuilder.build().toByteArray(),body,channel);

        //发送数据
        writeChannel(bb, channel);
    }

    /**
     * <pre>
     * 构建Buf数据包
     * 用于服务器和网关之间通讯
     * </pre>
     */
    public final static ByteBuf buildBuf(byte[] header, byte[] body, Channel channel) {
        if (body == null) {
            body = new byte[0];
        }
        int bodyLen  = 2 + header.length + body.length;

        int totalLen = 2 + bodyLen;
        // 整个包长度(用于处理粘包半包)
        if (totalLen > Short.MAX_VALUE) {
            Log.error("数据包len" + totalLen + "超过" + Short.MAX_VALUE + ",发送失败,len:" + totalLen + "\npacket:" + JsonUtil.stringify(body));
            return null;
        }
        ByteBuf buf;
        if (channel != null) {
            buf = channel.alloc().buffer(totalLen);
        } else {
            buf = Unpooled.buffer(totalLen);
        }

        //包头的两个字节
        buf.writeShortLE(bodyLen);
        // 包体前俩字节
        buf.writeShortLE(header.length);
        // 包体消息头
        buf.writeBytes(header);
        // 包体消息体
        buf.writeBytes(body);
        return buf;
    }

    /**
     * <pre>
     * 发送服务器间包
     * ！不等待发包完成
     * </pre>
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
     * 发包数统计
     */
    private final static AtomicLong counter = new AtomicLong();

    /**
     * <pre>
     * 等待连接可写
     * </pre>
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
