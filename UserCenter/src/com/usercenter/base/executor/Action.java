package com.usercenter.base.executor;

import javax.management.timer.Timer;

import com.utils.Log;
import com.utils.RandomStrUtil;
import com.utils.TimeUtil;


/**
 * <pre>
 * 执行action处理事件的基类
 * </pre>
 * 
 * @author reison
 */
public abstract class Action implements Runnable {

	/** action唯一Id */
	private final String actionId;

	/** 执行任务队列 */
	protected ActionQueue queue;

	/** 创建该执行任务的时间 */
	protected long createTime;

	/** 开始执行任务的时间 */
	protected long startTime;

	public Action(ActionQueue queue) {
		this.queue = queue;
		this.startTime = Long.MAX_VALUE;
		this.actionId = RandomStrUtil.randomStr(12);
		this.createTime = System.currentTimeMillis();
	}

	public final ActionQueue getActionQueue() {
		return queue;
	}

	@Override
	public final void run() {
		if (queue != null) {
			startTime = System.currentTimeMillis();
			try {
				// 执行
				execute();
				long end = System.currentTimeMillis();
				long interval = end - startTime;
				long leftTime = end - createTime;
				if (interval >= TimeUtil.COST_TIME_MAX_LOG) {
					// 记录执行时间
					Log.logTime(getLogName(), interval, leftTime, queue.getQueue().size(), getComment());
				}
			} catch (Exception e) {
				Log.error("run action execute exception. action : " + this.toString(), e);
			} finally {
				// 移除控制器缓存
				ActionFutureMgr.removeFuture(getActionId());
				// 移除自身，执行下一个
				queue.dequeue(this);
			}
		} else {
			Log.error("Action执行时，发现队列为空，action:" + this.getClass().getSimpleName());
		}
	}

	/**
	 * <pre>
	 * 子类重写执行任务逻辑
	 * </pre>
	 */
	public abstract void execute();

	/**
	 * <pre>
	 * toString
	 * </pre>
	 *
	 * @return
	 */
	public String getComment() {
		return this.toString();
	}

	/**
	 * <pre>
	 * 获取执行的action任务的类名
	 * </pre>
	 *
	 * @return
	 */
	public String getLogName() {
		return getClass().getSimpleName();
	}

	/**
	 * <pre>
	 * 重新设置队列 / 玩家
	 * </pre>
	 *
	 * @param queue
	 */
	public void setActionQueue(AbstractActionQueue queue) {
		this.queue = queue;
	}

	/**
	 * <pre>
	 * 获取执行的线程池
	 * </pre>
	 *
	 * @return
	 */
	public Executor getExecutor() {
		return this.queue.getExecutor();
	}

	/**
	 * <pre>
	 * 获取唯一Id
	 * </pre>
	 *
	 * @return
	 */
	public final String getActionId() {
		return actionId;
	}

	/** action超时检测毫秒数 */
	private final static int TIMEOUT_MILLS = (int) (Timer.ONE_SECOND * 30);

	/**
	 * <pre>
	 * Action执行超时检测
	 * </pre>
	 */
	public final void timeOutCheckStop() {
		long totalTime = System.currentTimeMillis() - startTime;
		if (totalTime >= TIMEOUT_MILLS) {
			// 记录执行时间
			Log.logTime(getLogName() + "-TimeOutActionRemove", totalTime, totalTime, queue.getQueue().size(), getComment());
			// 停止执行队列首，并且继续执行下一个action
			ActionFutureMgr.stopAction(getActionId());
		}
	}

}
