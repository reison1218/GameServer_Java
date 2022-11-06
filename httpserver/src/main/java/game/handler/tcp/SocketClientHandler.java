/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.handler.tcp;

import game.action.KeepLiveAction;
import game.action.ReConnectAction;
import game.base.executor.ExecutorMgr;
import game.base.packet.Packet;
import game.mgr.HttpServerMgr;
import game.protobuf.GPMsg.CommandType;
import game.protobuf.GPMsg.TMSG_HANDSHAKE_REQ;
import game.protobuf.GPMsg.TMSG_HEADER;
import game.protobuf.GPMsg.TMSG_OPERATION_EMAIL_REQ;
import game.utils.Log;
import game.utils.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <pre>
 * Socket客户端
 * </pre>
 *
 * @author reison
 * @time 2019年7月27日
 */
public class SocketClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.error("channelInactive连接断开了");
        handleDisconnect(ctx);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(ChannelHandlerContext,
     * Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("连接发生异常", cause);
        handleDisconnect(ctx);
    }

    /**
     * <pre>
     * 处理连接断开
     * </pre>
     */
    protected void handleDisconnect(ChannelHandlerContext ctx) {
        Log.error("handleDisconnect连接断开了");
        // 异步处理断开连接重连
        ExecutorMgr.getDefaultExecutor().enDelayQueue(new ReConnectAction(5000));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendHandShake(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        try {
            Packet packet = Packet.read(msg);
            TMSG_HEADER.Builder builder = TMSG_HEADER.newBuilder();
            builder.mergeFrom(packet.getHeader());
            switch (builder.getCommand()) {
                //如果是握手消息，就开始发送心跳，保持tcp连接
                case CommandType_HandShakeResponse:
                    keepLive(ctx.channel());

                    break;
                case CommandType_KeepaliveResponse:
                    Log.info("收到socketserver心跳包");
                    break;
                case CommandType_OperationEmail:
                    Log.info("收到邮件包");
                    mailHandler(packet, ctx.channel());
                    break;
            }
        } catch (Throwable e) {
            Log.error("网关socket客户端接收包解析异常", e);
        }
    }

    /**
     * 心跳包
     */
    private void keepLive(Channel channel) {
        KeepLiveAction action = new KeepLiveAction(channel, 3000);
        ExecutorMgr.getDefaultExecutor().enDelayQueue(action);
    }

    private void sendHandShake(Channel channel) {
        //消息体
        TMSG_HANDSHAKE_REQ.Builder bodyBuilder = TMSG_HANDSHAKE_REQ.newBuilder();
        bodyBuilder.setGameID(2141);
        for (int serverId : HttpServerMgr.getServerMap().keySet()) {
            bodyBuilder.addWorldIDs(serverId);
        }
        MessageUtil.sendMessage(CommandType.CommandType_HandShake, bodyBuilder.build().toByteArray(), channel);
    }

    private void mailHandler(Packet packet, Channel channel) {

        try {
            TMSG_OPERATION_EMAIL_REQ mailReqProto = TMSG_OPERATION_EMAIL_REQ.parseFrom(packet.getBody());

        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }
}
