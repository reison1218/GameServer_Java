/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.executor;

import java.lang.reflect.Method;

import game.utils.Log;


/**
 * <pre>
 * 通用Action执行器
 * </pre>
 * 
 * @author reison
 */
public final class CommonExecutor {

	private Executor executor;

	public CommonExecutor(int threadCount, String name) {
		if (threadCount < 1) {
			threadCount = 1;
		}
		if (threadCount > 16) {
			threadCount = 16;
		}
		this.executor = new Executor(threadCount, name);
		this.executor.preStart();
	}

	/**
	 * <pre>
	 * 在指定队列执行Action
	 * 玩家的队列和执行任务
	 * </pre>
	 * 
	 * @param queue 指定队列
	 * @param action 需执行action
	 */
	public void enqueue(ActionQueue queue, Action action) {
		if (queue == null || action == null) {
			return;
		}
		// 玩家队列传入一个线程池
		queue.setIfNotExecutor(executor);
		// 将执行任务添加到队列中
		queue.enqueue(action);
		// 当队列的任务超过500就会打印提示
		int size = queue.getQueue().size();
		if (size > 100) {
			Log.warn("队列" + queue.getClass().getSimpleName() + "长度超过100,size:" + size);
		}
	}

	/**
	 * <pre>
	 * 在默认队列执行Action
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param action 需执行action
	 */
	public void enqueue(Action action) {
		if (action == null) {
			return;
		}
		if (action.getActionQueue() == null) {
			action.setActionQueue(executor.getDefaultQueue());
			Log.warn("存在Action的队列为空，调用了强制设置Action队列为通用执行器默认队列！！！");
		}
		if (action.getActionQueue() != executor.getDefaultQueue()) {
			action.setActionQueue(executor.getDefaultQueue());
			Log.warn("Action队列和执行器默认队列不一致，调用了强制设置action队列为通用执行器默认队列！！！");
		}
		executor.enDefaultQueue(action);
		int size = executor.getDefaultQueue().getQueue().size();
		if (size > 500) {
			Log.warn("默认队列" + "长度超过500，size:" + size);
		}
	}

	/**
	 * <pre>
	 * 在指定队列执行延时Action
	 * </pre>
	 * 
	 * @param queue 指定队列
	 * @param action 需延迟执行action
	 */
	public void enDelayQueue(ActionQueue queue, DelayAction action) {
		if (queue == null || action == null) {
			return;
		}
		queue.setIfNotExecutor(executor);
		if (action.getExecTime() < System.currentTimeMillis()) {
			action.setExecTime(System.currentTimeMillis() + action.getDelay());
		}
		queue.enDelayQueue(action);
		int size = queue.getQueue().size();
		if (size > 500) {
			Log.warn("队列" + queue.getClass().getSimpleName() + "长度超过500，size:" + size);
		}
	}

	/**
	 * <pre>
	 * 在默认队列执行延时Action
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param action 需延迟执行action
	 */
	public void enDelayQueue(DelayAction action) {
		if (action == null) {
			return;
		}
		if (action.getExecTime() < System.currentTimeMillis()) {
			action.setExecTime(System.currentTimeMillis() + action.getDelay());
		}
		if (action.getActionQueue() == null) {
			action.setActionQueue(executor.getDefaultQueue());
			//Log.error("存在DelayAction的队列为空，调用了强制设置DelayAction队列为通用执行器默认队列！！！，打印堆栈：", new Exception());
		}
		if (action.getActionQueue() != executor.getDefaultQueue()) {
			action.setActionQueue(executor.getDefaultQueue());
			Log.error("DelayAction队列和执行器默认队列不一致，调用了强制设置DelayAction队列为通用执行器默认队列！！！，打印堆栈：", new Exception());
		}
		executor.enDelayQueue(action);
		int size = executor.getDefaultQueue().getQueue().size();
		if (size > 500) {
			Log.warn("默认队列" + "长度超过500，size:" + size);
		}
	}

	public ActionQueue getActionQueue() {
		return executor.getDefaultQueue();
	}

	/**
	 * <pre>
	 * 在默认队列执行静态函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param mth 静态方法
	 * @param args 函数的参数
	 */
	public <T> void execStatic(Method mth, Object... args) {
		exec(mth, null, args);
	}

	/**
	 * <pre>
	 * 在默认队列执行实例函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param mth 实例方法
	 * @param obj 实例方法所属对象
	 * @param args 方法参数
	 */
	public <T> void exec(Method mth, T obj, Object... args) {
		if (mth == null) {
			return;
		}
		try {
			enqueue(new CommonExecAction(ExecutorMgr.getDefaultExecutor().getActionQueue(), mth, obj, args));
		} catch (SecurityException e) {
			Log.error("", e);
		}
	}

	/**
	 * <pre>
	 * 在指定队列执行静态函数
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param queue 指定的队列
	 * @param mth 执行的静态方法
	 * @param args 方法参数
	 */
	public <T> void execStatic(ActionQueue queue, Method mth, Object... args) {
		exec(queue, mth, null, args);
	}

	/**
	 * <pre>
	 * 在指定队列执行实例函数
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param queue 指定队列
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param args 方法参数
	 */
	public <T> void exec(ActionQueue queue, Method mth, T obj, Object... args) {
		if (mth == null) {
			return;
		}
		try {
			enqueue(queue, new CommonExecAction(queue, mth, obj, args));
		} catch (SecurityException e) {
			Log.error("", e);
		}
	}

	/**************************** 循环执行函数 *****************************************/
	/**
	 * <pre>
	 * 在默认队列循环执行静态函数
	 * 立即执行一次(仅当delay>10000时生效)
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 循环毫秒数周期
	 * @param mth 静态方法
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execStaticRepeated(long delay, Method mth, Object... args) {
		return execStaticRepeated(delay, mth, true, args);
	}

	/**
	 * <pre>
	 * 在默认队列循环执行静态函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 循环毫秒数周期
	 * @param mth 静态方法
	 * @param instantOnce 是否立即执行一次(仅当delay>10000时生效)
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execStaticRepeated(long delay, Method mth, boolean instantOnce, Object... args) {
		return execRepeated(delay, true, mth, null, instantOnce, args);
	}

	/**
	 * <pre>
	 * 在默认队列循环执行实例函数
	 * 立即执行一次(仅当delay>10000时)
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param delay 循环毫秒数周期
	 * @param repeated 是否循环执行
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execRepeated(long delay, boolean repeated, Method mth, T obj, Object... args) {
		return execRepeated(delay, repeated, mth, obj, true, args);
	}

	/**
	 * <pre>
	 * 在默认队列循环执行实例函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param delay 循环毫秒数周期
	 * @param repeated 是否循环执行
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param instantOnce 是否立即执行一次(仅当delay>10000时生效)
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execRepeated(long delay, boolean repeated, Method mth, T obj, boolean instantOnce, Object... args) {
		if (mth == null) {
			return null;
		}
		try {
			if (delay > 10000 && instantOnce) {
				enqueue(new CommonExecAction(ExecutorMgr.getDefaultExecutor().getActionQueue(), mth, obj, args));
			}
			CommonDelayAction delayAction = new CommonDelayAction(delay, ExecutorMgr.getDefaultExecutor().getActionQueue(), repeated, mth, obj, args);
			enDelayQueue(delayAction);
			return delayAction;
		} catch (SecurityException e) {
			Log.error("", e);
		}
		return null;
	}

	/**
	 * <pre>
	 * 在指定队列循环执行静态函数
	 * 立即执行一次(仅当delay>10000时生效)
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 循环毫秒数周期
	 * @param queue 指定队列
	 * @param mth 静态方法
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execStaticRepeated(long delay, ActionQueue queue, Method mth, Object... args) {
		return execStaticRepeated(delay, queue, mth, true, args);
	}

	/**
	 * <pre>
	 * 在指定队列循环执行静态函数
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 循环毫秒数周期
	 * @param queue 指定队列
	 * @param mth 静态方法
	 * @param instantOnce 是否立即执行一次(仅当delay>10000时生效)
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execStaticRepeated(long delay, ActionQueue queue, Method mth, boolean instantOnce, Object... args) {
		return execRepeated(delay, queue, true, mth, null, instantOnce, args);
	}

	/**
	 * <pre>
	 * 在指定队列循环执行实例函数
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param delay 循环毫秒数周期
	 * @param queue 指定队列
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param instantOnce 是否立即执行一次(仅当delay>10000时生效)
	 * @param args 方法参数
	 */
	public <T> CommonDelayAction execRepeated(long delay, ActionQueue queue, boolean repeated, Method mth, T obj, boolean instantOnce, Object... args) {
		if (mth == null) {
			return null;
		}
		try {
			if (delay > 10000 && instantOnce) {
				enqueue(queue, new CommonExecAction(queue, mth, obj, args));
			}
			CommonDelayAction delayAction = new CommonDelayAction(delay, queue, repeated, mth, obj, args);
			enDelayQueue(queue, delayAction);
			return delayAction;
		} catch (SecurityException e) {
			Log.error("", e);
		}
		return null;
	}

	/**************************** 延时执行函数 *****************************************/
	/**
	 * <pre>
	 * 在默认队列延时执行静态函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 延迟毫秒数
	 * @param mth 静态方法
	 * @param args 方法参数
	 */
	public <T> void execStaticDelay(long delay, Method mth, Object... args) {
		execRepeated(delay, false, mth, null, false, args);
	}

	/**
	 * <pre>
	 * 在指定队列延时执行静态函数
	 * </pre>
	 * 
	 * @param <T> 参数类型
	 * @param delay 延迟毫秒数
	 * @param queue 指定队列
	 * @param mth 静态方法
	 * @param args 方法参数
	 */
	public <T> void execStaticDelay(long delay, ActionQueue queue, Method mth, Object... args) {
		execRepeated(delay, queue, false, mth, null, false, args);
	}

	/**
	 * <pre>
	 * 在默认队列延时执行实例函数
	 * ！默认队列指CommonExecutorMgr.getDefaultExecutor().getActionQueue()
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param delay 延迟毫秒数
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param args 方法参数
	 */
	public <T> void execDelay(long delay, Method mth, T obj, Object... args) {
		execRepeated(delay, false, mth, obj, false, args);
	}

	/**
	 * <pre>
	 * 在指定队列延时执行实例函数
	 * </pre>
	 * 
	 * @param <T> 对象类型或参数类型
	 * @param delay 延迟毫秒数
	 * @param queue 指定队列
	 * @param mth 实例方法
	 * @param obj 方法所属对象
	 * @param args 方法参数
	 */
	public <T> void execDelay(long delay, ActionQueue queue, Method mth, T obj, Object... args) {
		execRepeated(delay, queue, false, mth, obj, false, args);
	}

	public final Executor getExecutor() {
		return executor;
	}

}
