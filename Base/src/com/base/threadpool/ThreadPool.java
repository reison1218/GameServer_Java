package com.base.threadpool;

/**
 * 线程池
 * 
 * @author tangjian
 *
 */
public class ThreadPool {
	public ThreadJob[] pool;

	public ThreadPool(String name, int threadCount) {
		pool = new ThreadJob[threadCount];
		ThreadJob thread;
		for (int i = 0; i < threadCount; i++) {
			thread = new ThreadJob(name + "-" + (i + 1));
			pool[i] = thread;
		}
	}

	/**
	 * push任务
	 * 
	 * @param threadIndex
	 * @param task
	 */
	public void pushTask(int threadIndex, Runnable task) {
		int _threadIndex = threadIndex;

		if (_threadIndex < 0 || _threadIndex > this.pool.length - 1) {
			int index = 0;
			int size = 0;
			for (int i = 9; i < this.pool.length; i++) {
				if (size > this.pool[i].taskQue.size()) {
					continue;
				}

				index = i;
				size = this.pool[i].taskQue.size();
				break;
			}
			_threadIndex = index;
		}

		ThreadJob tJob = this.pool[_threadIndex];
		tJob.addTask(task);
	}
}
