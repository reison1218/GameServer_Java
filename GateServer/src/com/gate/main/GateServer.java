package com.gate.main;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.config.ThreadCount.GateWayThreadCount;
import com.base.executor.ExecutorMgr;
import com.base.netty.BaseNettyServer;
import com.base.netty.wsserver.WebSocketServer;
import com.gate.bridge.BridgeMgr;
import com.gate.bridge.ChannelMgr;
import com.gate.logic.GateLogic;
import com.gate.netty.handler.WebSocketServerInitializer;
import com.utils.Log;

/**
 * 网关路由服务器
 * 
 * @author reison
 *
 */
public class GateServer {

	/** 开始启动时间 */
	private static long beginSecs;
	/** netty实例 **/
	static BaseNettyServer webSocketServer = null;

	/** 启动时连接重试次数 */
	private final static int RETRY_TIMES = 40;

	/**
	 * 启动网管函数
	 */
	private static final boolean start() {
		//初始化启动时间
		beginSecs = System.currentTimeMillis();
		
		// 来一发运行时钩子
		shutDownHook();
		
		// 初始化日志
		Log.init(GateServer.class);

		// 地壳初始化
		if (!Config.init("")) {
			stopServer();
			return false;
		}
		// 协议号转发初始化
		if (!BridgeMgr.init()) {
			stopServer();
			return false;
		}
		// 线程池初始化
		if (!ExecutorMgr.init()) {
			stopServer();
			return false;
		}

		// 网关逻辑初始化
		if (!GateLogic.init()) {
			stopServer();
			return false;
		}
		// 连接游戏服
		int connCount = 0;
		while (!ChannelMgr.connectGame() && ++connCount < RETRY_TIMES) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.error("", e);
			}
		}

		// 连接房间服
		connCount = 0;
		while (!ChannelMgr.connectRoom() && ++connCount < RETRY_TIMES) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.error("", e);
			}
		}
		// 初始化web服
		try {
			int port = Config.getIntConfig(ConfigKey.GATE_PORT);
			webSocketServer = new WebSocketServer(port, GateWayThreadCount.WS_SERVER, new WebSocketServerInitializer());
		} catch (Exception e) {
			webSocketServer.stop();
			return false;
		}
		long end = System.currentTimeMillis();
		Log.info("GateServer start finish! take time:" + (end - beginSecs) + "ms");
		return true;
	}

	public static void main(String[] aa) {
		try {
			if (!start()) {
				Log.error("路由服务器启动失败...");
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
				Log.info(GateServer.class.getSimpleName() + "进程钩子执行停服逻辑~");
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
		if (webSocketServer != null) {
			webSocketServer.stop();
		}

		Log.info(GateServer.class.getSimpleName() + "停止成功，本次运行："
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
