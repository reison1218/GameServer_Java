/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * <pre>
 * Action执行管理(不包含DelayAction)
 * </pre>
 * 
 * @author reison
 */
public final class ActionFutureMgr {

	/** action执行返回的控制器Map */
	private final static Map<String, Future<?>> actionFutures = new ConcurrentHashMap<>();

	/**
	 * <pre>
	 * 添加控制器
	 * </pre>
	 *
	 * @param actionId
	 * @param f
	 */
	public final static void addFuture(String actionId, Future<?> f) {
		actionFutures.put(actionId, f);
	}

	/**
	 * <pre>
	 * 移除控制器
	 * </pre>
	 *
	 * @param actionId
	 */
	public final static void removeFuture(String actionId) {
		actionFutures.remove(actionId);
	}

	/**
	 * <pre>
	 * 停止某Action
	 * </pre>
	 *
	 * @param actionId
	 * @return
	 */
	public final static boolean stopAction(String actionId) {
		Future<?> f = actionFutures.get(actionId);
		if (f != null && !f.isDone()) {
			return f.cancel(true);
		}
		return false;
	}

}
