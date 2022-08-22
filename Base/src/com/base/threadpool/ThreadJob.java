package com.base.threadpool;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 工作线程，每一个都是单线程
 * 
 * @author tangjian
 *
 */
public class ThreadJob {

	/**
	 * 线程
	 */
	Thread thread;

	/**
	 * 任务队列
	 */
	LinkedBlockingQueue<Runnable> taskQue;

	public ThreadJob(String name) {

		// 初始化任务队列
		taskQue = new LinkedBlockingQueue<Runnable>();
		// 初始化执行的单线程
		thread = new Thread(() -> {
			work();
		}, name);
		thread.start();
	}

	/**
	 * 开始工作，从队列里面拿出任务执行
	 */
	public void work() {
		try {
			for (;;) {
				Runnable task = taskQue.take();
				task.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加任务
	 * 
	 * @param task
	 */
	public void addTask(Runnable task) {
		this.taskQue.offer(task);
	}
}
