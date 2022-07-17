package com.base.threadpool;

import java.util.Vector;

import com.utils.Log;

/**
 * 线程池
 * @author tangjian
 *
 */
public class ThreadPool {
	Vector<ThreadJob> pool;

	public ThreadPool(String name, int threadCount) {
		pool = new Vector<>(threadCount);
		ThreadJob thread;
		for (int i = 0; i < threadCount; i++) {
			thread = new ThreadJob(name + "-" + (i + 1));
			pool.add(thread);
		}
	}

	/**
	 * push任务
	 * @param threadIndex
	 * @param task
	 */
	public void pushTask(int threadIndex, Task task) {
		ThreadJob tJob = this.pool.get(threadIndex);
		if (tJob == null) {
			Log.error("there is no thread for index:" + threadIndex);
			return;
		}
		tJob.addTask(task);
	}
}
