package game.base.threadpool;

import game.utils.RandomUtil;

/**
 * 线程池
 *
 * @author tangjian
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
     */
    public void pushTask(int threadIndex, Runnable task) {
        int _threadIndex = threadIndex;

        if (_threadIndex < 0 || _threadIndex > this.pool.length - 1) {
            _threadIndex = 0;
        }
        ThreadJob tJob = this.pool[_threadIndex];
        tJob.addTask(task);
    }

    public void pushTask(Runnable task){
        int threadIndex = RandomUtil.rand(0,this.pool.length);
        pushTask(threadIndex,task);
    }
}