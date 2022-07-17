package com.base.threadpool;

/**
 * 任务类
 * 
 * @author tangjian
 *
 */
public abstract class Task implements Runnable {
	@Override
	public void run() {
		execute();
	}

	/**
	 * 执行函数
	 */
	public abstract void execute();
}
