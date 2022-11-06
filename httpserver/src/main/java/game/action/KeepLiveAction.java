package game.action;

import game.base.executor.DelayAction;
import game.base.executor.ExecutorMgr;
import game.protobuf.GPMsg.CommandType;
import game.protobuf.GPMsg.TMSG_HEADER;
import game.utils.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author tangjian
 * @date 2022-10-27 15:15
 * desc
 */
public class KeepLiveAction extends DelayAction {
    Channel channel;

    public KeepLiveAction(Channel channel, long delay) {
        super(ExecutorMgr.getDefaultExecutor().getActionQueue(), delay);
        this.channel = channel;
    }

    /**
     *
     */
    @Override
    public void execute() {
        if (!this.channel.isActive()) {
            return;
        }
        MessageUtil.sendMessage(CommandType.CommandType_Keepalive,null,channel);
        enDelayQueue();
    }
}
