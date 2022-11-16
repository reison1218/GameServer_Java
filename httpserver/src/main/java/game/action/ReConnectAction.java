/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.action;


import game.HttpServer;
import game.base.executor.DelayAction;
import game.base.executor.ExecutorMgr;

/**
 * <pre>
 * 游戏服重连Action
 * </pre>
 *
 * @author reison
 * @time 2019年7月27日
 */
public final class ReConnectAction extends DelayAction {

    public ReConnectAction(long delay) {
        super(ExecutorMgr.getDefaultExecutor().getActionQueue(), delay);
    }

    /**
     *
     */
    @Override
    public void execute() {
        if (!HttpServer.reconnSocketServer()) {
            enDelayQueue();
        }
    }

}
