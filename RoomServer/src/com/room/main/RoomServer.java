package com.room.main;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.config.ThreadCount.GameThreadCount;
import com.base.executor.ExecutorMgr;
import com.base.netty.BaseNettyServer;
import com.base.netty.socketserver.SocketServer;
import com.base.template.mgr.TemplateMgr;
import com.utils.Log;
import com.room.netty.handler.RoomSocketServerInitializer;

/**
 * 房间服务器
 * 
 * @author reison
 *
 */
public class RoomServer {
	/** 开始启动时间 */
	private static long beginSecs;
	/** netty实例 **/
	static BaseNettyServer socketServer = null;

	private static final boolean start() {
		beginSecs = System.currentTimeMillis();
		// 初始化运行时钩子
		shutDownHook();
		// 初始化日志
		if (!Log.init(RoomServer.class)) {
			stopServer();
			return false;
		}
		// 初始化配置文件

		// 地壳初始化
		if (!Config.init("")) {
			stopServer();
			return false;
		}
		// 线程池初始化
		if (!ExecutorMgr.init()) {
			stopServer();
			return false;
		}
		// 配置表初始化
		if (!TemplateMgr.init()) {
			stopServer();
			return false;
		}
		// 网络初始化
		try {
			int port = Config.getIntConfig(ConfigKey.ROOM_PORT);
			socketServer = new SocketServer(port, GameThreadCount.SOCKET_SERVER, new RoomSocketServerInitializer());
		} catch (Exception e) {
			stopServer();
			return false;
		}
		long end = System.currentTimeMillis();
		Log.info("RoomServer start finish! take time:" + (end - beginSecs) + "ms");
		return true;
	}

	public static void main(String[] aa) {
		try {
			if (!start()) {
				Log.error("房间服务器启动失败...");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			stopServer();
		}
	}

	/**
	 * 运行时钩子，当jvm退出时候调用
	 */
	public static void shutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Log.info(RoomServer.class.getSimpleName() + "进程钩子执行停服逻辑~");
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

		// 停服前逻辑
		beforeShutDown();
		if (socketServer != null) {
			socketServer.stop();
		}

		Log.info(RoomServer.class.getSimpleName() + "停止成功，本次运行："
				+ (int) Math.ceil((System.currentTimeMillis() / 1000 - getBeginSecs() / 1000) / 60 + 0.1f) + "分钟");
		// 停止文件日志模块(！！！放到最后)
		// LogManager.shutdown();
		System.exit(0);
		return true;
	}

	public static long getBeginSecs() {
		return beginSecs;
	}

	public static void beforeShutDown() {
		// TODO
	}
}
