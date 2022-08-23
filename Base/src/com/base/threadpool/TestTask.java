package com.base.threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.sun.java_cup.internal.runtime.virtual_parse_stack;
import com.utils.RandomUtil;

public class TestTask extends Task {

	List<Integer> list = new ArrayList<>();

	TestTask task;

	public TestTask(TestTask task) {
		this.task = task;
	}

	public TestTask() {
	}

	@Override
	public void execute() {
		Collections.sort(this.task.list);
	}

	public static void main(String[] args) {
//		TestTask task = new TestTask();
//		for (int i = 0; i < 9999999; i++) {
//			task.list.add(RandomUtil.rand(99999));
//		}
//		long start = System.currentTimeMillis();
//		Collections.sort(task.list);
//		long res = System.currentTimeMillis() - start;
//		System.out.println("time:" + res + "ms");

		testThread();
//
		testSync();

	}

	public static void testSync() {
		TestTask task = new TestTask();
		for (int i = 0; i < 9999999; i++) {
			task.list.add(RandomUtil.rand(99999));
		}
		List<Thread> tList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					task.sort();
				}
			});
			tList.add(thread);
		}
		long start = System.currentTimeMillis();
		for (Thread tread : tList) {
			tread.start();
			try {
				tread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long res = System.currentTimeMillis() - start;
		System.out.println("sync-time:" + res + "ms");
	}

	public static void testThread() {
		ThreadPool pool = new ThreadPool("test", 1);
		
		TestTask task = new TestTask();
		for (int i = 0; i < 9999999; i++) {
			task.list.add(RandomUtil.rand(99999));
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			TestTask _task = new TestTask(task);
			pool.pushTask(0, _task);
		}

		try {
			for(;;) {
				boolean res = pool.pool[0].taskQue.isEmpty();
				if(res)
					break;
			}
			long res = System.currentTimeMillis() - start;
			System.out.println("thread-time:" + res + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test() {
		ThreadPool pool = new ThreadPool("test", 8);
		pool.pushTask(Integer.MAX_VALUE, ()->{
			System.out.print("test");
		});
	}

	public synchronized void sort() {
		Collections.sort(list);
		
	}

}
