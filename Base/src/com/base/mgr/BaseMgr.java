/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.mgr;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.utils.Log;
import com.utils.RandomUtil;


/**
 * <pre>
 * 实例管理基类
 * 1、封装K-V缓存的基本操作
 * 2、支持过期淘汰数据
 * </pre>
 * 
 * @author reison
 * @time 2018年7月7日 上午9:35:23
 */
public abstract class BaseMgr<K, V> {

	/** 检测间隔 */
	protected final static int CHECK_INTERVAL_MILLS = 1000 * 60 * 3;

	/** 检测执行器 */
	protected static ScheduledExecutorService scheduler = null;

	static {
		scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "BaseMgrCheckThread");
			}
		});
	}

	/** 实例实例缓存<Key,Instance>-只能在本类中使用,因为需要控制容量 */
	private final Map<K, V> instancePool = new ConcurrentHashMap<>();

	public BaseMgr() {
		this(false);
	}

	public BaseMgr(boolean checkExpire) {
		if (checkExpire) {
			long chkMills = CHECK_INTERVAL_MILLS;
			if (chkIntervalMills() > 0) {
				chkMills = chkIntervalMills();
			}
			int randomMills = 30000;
			if (chkMills < randomMills) {
				chkMills = randomMills;
			}
			scheduler.scheduleWithFixedDelay(new CheckExpireTask<>(this), chkMills + RandomUtil.rand(-randomMills, randomMills), chkMills, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * <pre>
	 * 检测间隔毫秒数
	 * ！需要检测过期的，子类需重写
	 * </pre>
	 *
	 * @return
	 */
	protected long chkIntervalMills() {
		return 0;
	}

	/**
	 * <pre>
	 * 初始化
	 * </pre>
	 *
	 * @return
	 */
	public boolean init() {
		return true;
	}

	/**
	 * <pre>
	 * 创建实例
	 * </pre>
	 *
	 * @param key 实例关联的key
	 * @param clazz 实例关联的类
	 * @param params 实例关联的类构造器参数类数组
	 * @param args 实例关联的类构造器参数 数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final V createInstance(K key, Class<? extends V> clazz, Class<?>[] params, Object... args) {
		V instance = null;
		try {
			Constructor<?> contr = clazz.getConstructor(params);
			if (contr != null) {
				instance = (V) contr.newInstance(args);
			}
		} catch (Exception e) {
			Log.error("创建实例异常", e);
		}
		if (instance == null) {
			return null;
		}
		instancePool.put(key, instance);
		return instance;
	}

	/**
	 * <pre>
	 * 获取实例
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public final V get(K key) {
		return instancePool.get(key);
	}

	/**
	 * <pre>
	 * 是否存在Key
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public final boolean containsKey(K key) {
		return instancePool.containsKey(key);
	}

	/**
	 * <pre>
	 * 移除实例
	 * </pre>
	 *
	 * @param key 实例模板Id
	 * @return
	 */
	public final V remove(K key) {
		return instancePool.remove(key);
	}

	/**
	 * <pre>
	 * 设置实例
	 * </pre>
	 *
	 * @param key 实例模板Id
	 * @param value 实例
	 * @return
	 */
	public final void put(K key, V value) {
		instancePool.put(key, value);
	}

	/**
	 * <pre>
	 * 清空所有实例
	 * </pre>
	 */
	public final void clearAll() {
		instancePool.clear();
	}

	/**
	 * <pre>
	 * 是否为空
	 * </pre>
	 *
	 * @return
	 */
	public final boolean isEmpty() {
		return instancePool.isEmpty();
	}

	/**
	 * <pre>
	 * 获取所有实例Map的副本
	 * </pre>
	 *
	 * @return
	 */
	public final Map<K, V> getAllOfCopy() {
		return new ConcurrentHashMap<>(instancePool);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + "]:" + instancePool.size();
	}

	/**
	 * <pre>
	 * 检测过期
	 * </pre>
	 * 
	 * @author reison
	 * @time 2018年10月17日 下午5:47:12
	 */
	protected static class CheckExpireTask<K, V> implements Runnable {

		private BaseMgr<K, V> mgr;

		public CheckExpireTask(BaseMgr<K, V> mgr) {
			this.mgr = mgr;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long st = System.currentTimeMillis();
			try {
			} catch (Throwable e) {
				Log.error("", e);
			} finally {
				long ct = System.currentTimeMillis() - st;
				if (ct > 10) {
					Log.error(this.getClass().getSimpleName() + "-CostLongTime:" + ct + "ms " + mgr.toString());
				}
			}

		}

	}

}
