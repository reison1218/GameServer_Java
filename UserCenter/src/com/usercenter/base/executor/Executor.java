package com.usercenter.base.executor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.utils.Log;

/**
 * 线程池执行器
 * 
 * @author reison
 *
 */
public class Executor {

	private AbstractActionQueue defaultQueue; // 执行任务队列
	private ThreadPoolExecutor pool; // 线程池
	private DelayCheckThread delayCheckThread; // 延迟执行线程

	public Executor(int poolSize, String prefix) {
		TimeUnit unit = TimeUnit.MINUTES;
		LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
		if (prefix == null) {
			prefix = "";
		}
		ThreadFactory threadFactory = new Threads(prefix);
		// 因为队列无界，所以maxPoolSize，keepAliveTime无效
		pool = new ThreadPoolExecutor(poolSize, poolSize, 0, unit, workQueue, threadFactory, handler);
		defaultQueue = new AbstractActionQueue(this);

		delayCheckThread = new DelayCheckThread(prefix);
		delayCheckThread.start();
	}

	public AbstractActionQueue getDefaultQueue() {
		return defaultQueue;
	}

	/**
	 * 执行action 将action放入线程池执行
	 * 
	 * @param action
	 */
	public void enDefaultQueue(Action action) {
		defaultQueue.enqueue(action);
	}

	/**
	 * <pre>
	 * 执行任务
	 * </pre>
	 *
	 * @param action 任务action
	 */
	public Future<?> submit(Action action) {
		Future<?> f = pool.submit(action);
		if (!(action instanceof DelayAction)) {
			ActionFutureMgr.addFuture(action.getActionId(), f);
		}
		return f;
	}

	/**
	 * <pre>
	 * 执行延迟任务
	 * </pre>
	 *
	 * @param delayAction
	 */
	public void enDelayQueue(DelayAction delayAction) {
		delayCheckThread.addAction(delayAction);
	}

	public void stop() {
		delayCheckThread.stopping();
		if (!pool.isShutdown()) {
			pool.shutdown();
		}

	}

	public void preStart() {
		this.pool.prestartAllCoreThreads();
	}

	static class Threads implements ThreadFactory {

		static final AtomicInteger poolNumber = new AtomicInteger(1);
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;

		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(group, runnable,
					(new StringBuilder()).append(namePrefix).append(threadNumber.getAndIncrement()).toString(), 0L);
			if (thread.isDaemon())
				thread.setDaemon(false);
			if (thread.getPriority() != 5)
				thread.setPriority(5);
			return thread;
		}

		Threads(String prefix) {
			// 返回一个安全管理类
			SecurityManager securitymanager = System.getSecurityManager();
			// 当前线程的线程组
			group = securitymanager == null ? Thread.currentThread().getThreadGroup()
					: securitymanager.getThreadGroup();
			namePrefix = (new StringBuilder()).append("Pool-").append(poolNumber.getAndIncrement()).append("-")
					.append(prefix).append("-thread-").toString();
		}
	}

	/**
	 * <pre>
	 * 延迟执行线程
	 * </pre>
	 * 
	 * @author reison
	 * @time 2017年4月23日 上午11:15:52
	 */
	class DelayCheckThread extends Thread {

		private static final int FRAME_PER_SECOND = 120;
		private Object lock = new Object(); // 线程锁
		private List<DelayAction> queue; // 添加任务队列
		private List<DelayAction> execQueue; // 执行队列(复制queue中的任务到该执行队列中)
		private boolean isRunning;

		public DelayCheckThread(String prefix) {
			super(prefix + "DelayCheckThread");
			queue = new ArrayList<DelayAction>();
			execQueue = new LinkedList<DelayAction>();
			isRunning = true;
			setPriority(Thread.MAX_PRIORITY); // 给予高优先级
		}

		/** 获取该延迟执行线程是否执行 */
		public boolean isRunning() {
			return isRunning;
		}

		/** 设置该延迟线程不执行 */
		public void stopping() {
			if (isRunning) {
				isRunning = false;
			}
		}

		@Override
		public void run() {
			long balance = 0;
			while (isRunning) {
				try {
					int execute = 0;
					// 读取待执行的队列,如果没有可以执行的动作则等待
					poll();
					if (execQueue.size() == 0) {
						continue;
					}

					long start = System.currentTimeMillis();
					// 执行任务
					execute = execActions();
					execQueue.clear();
					long end = System.currentTimeMillis();
					long interval = end - start;
					balance += FRAME_PER_SECOND - interval;
					if (interval > FRAME_PER_SECOND) {
						Log.warn("DelayCheckThread is spent too much time: " + interval + "ms, execute = " + execute);
					}
					if (balance > 0) {
						Thread.sleep((int) balance);
						balance = 0;
					} else {
						if (balance < -1000) {
							balance += 1000;
						}
					}
				} catch (Exception e) {
					Log.error("DelayCheckThread error. ", e);
				}
			}
		}

		/**
		 * 返回执行成功的Action数量
		 **/
		public int execActions() {
			int executeCount = 0;
			for (DelayAction delayAction : execQueue) {
				try {
					long begin = System.currentTimeMillis();
					if (delayAction == null) {
						Log.error("error");
						continue;
					}
					if (!delayAction.canExec(begin)) {
						addAction(delayAction);
					}
					executeCount++;
					long end = System.currentTimeMillis();
					if (end - begin > FRAME_PER_SECOND) {
						Log.warn(delayAction.toString() + " spent too much time. time :" + (end - begin));
					}
				} catch (Exception e) {
					Log.error("执行action异常" + delayAction.toString(), e);
				}
			}
			return executeCount;
		}

		/**
		 * 添加Action到队列
		 * 
		 * @param delayAction
		 */
		public void addAction(DelayAction delayAction) {
			synchronized (lock) {
				queue.add(delayAction);
				lock.notifyAll();
			}
		}

		/**
		 * 以阻塞的方式读取队列,如果队列为空则阻塞
		 * 
		 * @throws InterruptedException
		 **/
		private void poll() throws InterruptedException {
			synchronized (lock) {
				if (queue.isEmpty()) {
					lock.wait();
				} else {
					execQueue.addAll(queue);
					queue.clear();
					lock.notifyAll();
				}
			}
		}
	}
}
