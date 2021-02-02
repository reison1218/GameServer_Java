package com.usercenter.main;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import com.usercenter.base.config.Config;
import com.usercenter.base.config.ConfigKey;
import com.usercenter.base.db.HikariDBPool;
import com.usercenter.base.executor.ExecutorMgr;
import com.usercenter.handler.ServerInfoListHandler;
import com.usercenter.handler.UserIdHandler;
import com.usercenter.handler.UserStateHandler;
import com.usercenter.mgr.SeasonMgr;
import com.usercenter.mgr.TimeTaskMgr;
import com.usercenter.mgr.UserCenterMgr;
import com.usercenter.redis.RedisPool;
import com.usercenter.template.mgr.TemplateMgr;
import com.utils.Log;

import sun.misc.Unsafe;

/**
 * 充值服务器
 * 
 * @author reison
 *
 */
public class UserCenterServer {

	static Server rs = new Server();

	/** 开始启动时间 */
	private static long beginSecs;

	/**
	 * 开始函数
	 * 
	 * @return
	 */
	private static boolean start() {
		beginSecs = System.currentTimeMillis();
		if (!Log.init(UserCenterServer.class)) {
			return false;
		}
		// 初始化配置
		if (!Config.init("")) {
			return false;
		}
		// 初始化db
		if (!HikariDBPool.init()) {
			return false;
		}
		// 初始化配置
		if (!TemplateMgr.init()) {
			return false;
		}
		// 初始化线程池
		if (!ExecutorMgr.init()) {
			return false;
		}
		// 初始化redis
		if (!RedisPool.init()) {
			return false;
		}
		// 初始化mgr
		if (!UserCenterMgr.init()) {
			return false;
		}
		// 定时器初始化
		if (!TimeTaskMgr.init()) {
			return false;
		}
		// 初始化赛季
		if (!SeasonMgr.init()) {
			return false;
		}
		// 初始化jetty
		try {
			ServerConnector connector = new ServerConnector(rs);
			int port = Config.getConfig(ConfigKey.HTTP);
			connector.setPort(port);
			rs.setConnectors(new Connector[] { connector });
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { new UserIdHandler(), new ServerInfoListHandler(),
					new UserStateHandler(), new DefaultHandler() });
			rs.setHandler(handlers);
			// 启动服务器
			rs.start();
			Log.info("用户中心启动完成，耗时：" + (System.currentTimeMillis() - beginSecs) + "ms,监听端口：" + 8888);
			rs.join();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 运行时钩子，当jvm退出时候调用
	 */
	public static void shutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Log.info(UserCenterServer.class.getSimpleName() + "进程钩子执行停服逻辑~");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.error("", e);
				} finally {
					stopServer();
				}
			}
		});
	}

	/**
	 * 停服
	 * 
	 * @return
	 */
	public static boolean stopServer() {
		HikariDBPool.stop();
		Log.info(UserCenterServer.class.getSimpleName() + "停止成功，本次运行："
				+ (int) Math.ceil((System.currentTimeMillis() / 1000 - getBeginSecs() / 1000) / 60 + 0.1f) + "分钟");
		System.exit(0);
		return true;
	}

	public static long getBeginSecs() {
		return beginSecs;
	}

	public static void main(String[] aa) {
		try {
			if (!start()) {
				Log.error("用户中心启动失败...");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			stopServer();
		}
	}

}
