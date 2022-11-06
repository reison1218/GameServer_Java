package game.base.executor;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.utils.Log;


/**
 * <pre>
 * 玩家类和各模块的顶层父类
 * 处理任务事件
 * </pre>
 * 
 * @author reison
 */
public abstract class AbstractEvent {
	// 监听器
	private Map<Integer, Collection<ObjectListener>> listeners;
	private static boolean isDebug = false;
	// 锁
	private Object lock = new Object();

	/**
	 * <pre>
	 * 添加一个事件 / 监听
	 * </pre>
	 *
	 * @param objectListener 监听器
	 * @param eventType 事件类型
	 */
	public void addListener(ObjectListener objectListener, int eventType) {
		synchronized (lock) {
			if (listeners == null) {
				listeners = new ConcurrentHashMap<Integer, Collection<ObjectListener>>();
			}
			// 判断有没有该事件
			Collection<ObjectListener> lsnSet = listeners.get(eventType);
			if (lsnSet == null) {
				// 创建一个listener放入到注册器中
				lsnSet = new HashSet<ObjectListener>();
				listeners.put(eventType, lsnSet);
			}
			lsnSet.add(objectListener);
			debugEventMsg("注册一个事件,类型为" + eventType);
		}
	}

	/**
	 * <pre>
	 * 移除一个事件 / 监听
	 * </pre>
	 *
	 * @param objectListener
	 * @param eventType
	 */
	public void removeListener(ObjectListener objectListener, int eventType) {
		synchronized (lock) {
			if (listeners == null) {
				return;
			}
			Collection<ObjectListener> tempInfo = listeners.get(eventType);
			if (tempInfo != null) {
				tempInfo.remove(objectListener);
			}
		}
		debugEventMsg("移除一个事件,类型为" + eventType);
	}

	/**
	 * <pre>
	 * 通知事件监听器,有新的时间
	 * </pre>
	 *
	 * @param event
	 */
	public void notifyListeners(ObjectEvent event) {
		try {
			// 监听器
			List<ObjectListener> tempList = null;
			synchronized (lock) {
				if (listeners == null) {
					return;
				}
				// 过滤主类型
				Collection<ObjectListener> lsnSet = listeners.get(event.getEventType());
				if (lsnSet != null) {
					tempList = new LinkedList<ObjectListener>();
					tempList.addAll(lsnSet);
				}
			}
			// 触发所有为该类型的监听器
			if (tempList != null) {
				for (ObjectListener tempLster : tempList) {
					tempLster.onEvent(event);
				}
			}
		} catch (Exception e) {
			Log.error("触发事件监听报错, event:" + event.toString(), e);
		}
	}

	/**
	 * <pre>
	 * 清除所有事件 / 监听
	 * </pre>
	 */
	public void clearListener() {
		synchronized (lock) {
			if (listeners != null) {
				listeners = null;
			}
		}
	}

	public void debugEventMsg(String msg) {
		if (isDebug) {
			Log.info(msg);
		}
	}
}
