package com.base.executor;

import java.util.LinkedList;
import java.util.Queue;

import com.utils.Log;


/**
 * <pre>
 * 抽象执行队列
 * </pre>
 * 
 * @author reison
 * @time 2017年4月22日
 */
public class AbstractActionQueue extends AbstractEvent implements ActionQueue {

	private Queue<Action> queue; // 执行队列
	private Executor executor; // 线程池

	public AbstractActionQueue(Executor executor) {
		this.executor = executor;
		queue = new LinkedList<Action>();
	}

	public AbstractActionQueue(Executor executor, Queue<Action> queue) {
		this.executor = executor;
		this.queue = queue;
	}

	public ActionQueue getActionQueue() {
		return this;
	}

	public Queue<Action> getQueue() {
		return queue;
	}

	/**
	 * 执行延迟的线程
	 * 
	 * @param delayAction
	 */
	public void enDelayQueue(DelayAction delayAction) {
		executor.enDelayQueue(delayAction);
	}

	/**
	 * 将任务事件添加到队列中
	 * 
	 * @param action
	 */
	public void enqueue(Action action) {
		int queueSize = 0;
		synchronized (queue) {
			queue.add(action);
			queueSize = queue.size();
		}
		// 当队列中只有1个action则立即执行
		if (queueSize == 1) {
			executor.submit(action);
		}
		// 队列>1个action，则检测队列首执行是否超时
		else if (queueSize > 1) {
			Action tempAcion = queue.peek();
			if (tempAcion != null) {
				tempAcion.timeOutCheckStop();
			}
		}
		if (queueSize > 1000) {
			Log.fatal(action.toString() + " queue size : " + queueSize);
		}
	}

	/**
	 * 执行下一个action
	 * 
	 * @param action
	 */
	public void dequeue(Action action) {
		int queueSize = 0;
		String tmpString = null;
		Action nextAction = null;
		synchronized (queue) {
			queueSize = queue.size();
			if (queueSize > 0) {
				Action temp = queue.remove();
				if (temp != action) {
					tmpString = temp.toString();

				}
				nextAction = queue.peek();
			}
		}

		if (nextAction != null) {
			executor.submit(nextAction);
		}
		if (queueSize == 0) {
			Log.fatal("queue.size() is 0,queue:" + this.getClass().getSimpleName() + ",action:" + action.getClass().getSimpleName());
		}
		if (tmpString != null) {
			Log.fatal("action queue error. temp " + tmpString + ", action : " + action.toString());
		}
	}

	public void clear() {
		synchronized (queue) {
			queue.clear();
		}
	}

	/**
	 * @param 线程池
	 */
	@Override
	public final void setIfNotExecutor(Executor executor) {
		if (this.executor == null) {
			this.executor = executor;
		}
	}

	public boolean isExecutorInit() {
		return this.executor != null;
	}

	/**
	 * @return
	 * @see com.hitalk.h5.base.executor.ActionQueue#getExecutor()
	 */
	@Override
	public Executor getExecutor() {
		return this.executor;
	}

}
