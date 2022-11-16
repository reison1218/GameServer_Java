package game.base.executor;

/**
 * <pre>
 * 延时执行的Action
 * </pre>
 * 
 * @author reison
 */
public abstract class DelayAction extends Action {
	protected long delay; // 延迟执行的时间

	protected long execTime; // 开始执行的时间

	public DelayAction(ActionQueue queue, long delay) {
		super(queue);
		this.delay = delay;
		execTime = System.currentTimeMillis() + delay;
	}

	// 现在的时间
	public boolean canExec(long curTime) {
		// 达到执行的时间
		if (curTime >= execTime) {
			// 开始执行
			createTime = curTime;
			// 将任务添加到队列中
			getActionQueue().enqueue(this);
			return true;
		}
		return false;
	}

	public final long getDelay() {
		return delay;
	}

	public final void setDelay(long delay) {
		this.delay = delay;
	}

	public final long getExecTime() {
		return execTime;
	}

	public final void addExecTime(long execTime) {
		this.execTime += execTime;
	}

	public final void setExecTime(long execTime) {
		this.execTime = execTime;
	}

	/**
	 * <pre>
	 * 多次循环延迟执行的action方法
	 * </pre>
	 */
	protected void enDelayQueue() {
		execTime = System.currentTimeMillis() + delay;
		getActionQueue().enDelayQueue(this);
	}
}
