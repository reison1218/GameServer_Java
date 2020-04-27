package com.game.main;

import com.base.config.Config;
import com.base.config.ConfigKey;
import com.base.config.ThreadCount.GameThreadCount;
import com.base.dbpool.HikariDBPool;
import com.base.executor.ExecutorMgr;
import com.base.module.ModuleMgr;
import com.base.netty.BaseNettyServer;
import com.base.netty.handler.HttpServerHandler;
import com.base.netty.handler.HttpServerInitializer;
import com.base.netty.httpserver.HttpServer;
import com.base.netty.socketserver.SocketServer;
import com.base.redis.RedisPool;
import com.base.template.mgr.TemplateMgr;
import com.base.type.ServerStatus;
import com.game.code.CodeMgr;
import com.game.mgr.GameMgr;
import com.game.mgr.TimeTaskMgr;
import com.game.netty.handler.GameSocketServerInitializer;
import com.game.player.AbstractPlayer;
import com.utils.Log;

/**
 * 游戏服务器
 * 
 * @author reison
 *
 */
public class GameServer {

	/** 开始启动时间 */
	private static long beginSecs;

	/** 当前nettyserver实例 */
	static BaseNettyServer socketServer = null;

	/** http服务器(接收管理员命令) */
	static BaseNettyServer httpServer;

	private static boolean start() {
		// 初始化启动时间
		beginSecs = System.currentTimeMillis();

		// 来一发运行时挂掉钩子
		//shutDownHook();

		// 初始化日志
		if (!Log.init(GameServer.class)) {
			stopServer();
			return false;
		}

		// 配置文件初始化
		if (!Config.init("")) {
			stopServer();
			return false;
		}

		// 配置表初始化
		if (!TemplateMgr.init()) {
			stopServer();
			return false;
		}

		// DB连接池
		if (!HikariDBPool.init()) {
			stopServer();
			return false;
		}

		// 初始化redis
		if (!RedisPool.init()) {
			stopServer();
			return false;
		}

		// 检测玩家基础表是否已创建
		if (!AbstractPlayer.init()) {
			stopServer();
			return false;
		}

		// 初始化命令mgr
		if (!CodeMgr.init()) {
			stopServer();
			return false;
		}

		// 模块管理初始化
		if (!ModuleMgr.init()) {
			stopServer();
			return false;
		}

		// 线程池初始化
		if (!ExecutorMgr.init()) {
			stopServer();
			return false;
		}

		// 定时器初始化
		if (!TimeTaskMgr.init()) {
			stopServer();
			return false;
		}
		// 初始化http服务
		initHttpServer();

		// 网络初始化
		try {
			int port = Config.getIntConfig(ConfigKey.GAME_PORT);
			socketServer = new SocketServer(port, GameThreadCount.SOCKET_SERVER, new GameSocketServerInitializer());
		} catch (Exception e) {
			stopServer();
			return false;
		}
		GameMgr.server_status = ServerStatus.RUNNING;
		Log.info("GameServer start finish! take time:" + (System.currentTimeMillis() - getBeginSecs()) + "ms");
		return true;
	}

	/**
	 * 初始化http服务
	 */
	public static void initHttpServer() {
		int port = Config.getIntConfig(ConfigKey.HTTP_PORT);
		httpServer = new HttpServer(port, GameThreadCount.HTTP_SERVER,
				new HttpServerInitializer(null, new HttpServerHandler()));
		if (httpServer == null) {
			stopServer();
		}
	}

	public static void main(String[] aa) {
		try {
			if (!start()) {
				Log.error("游戏服务器启动失败...");
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
				Log.info(GameServer.class.getSimpleName() + "进程钩子执行停服逻辑~");
				GameMgr.save();
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
		if (httpServer != null) {
			httpServer.stop();
		}
		HikariDBPool.stop();

		ExecutorMgr.stop();

		Log.info(GameServer.class.getSimpleName() + "停止成功，本次运行："
				+ (int) Math.ceil((System.currentTimeMillis() / 1000 - getBeginSecs() / 1000) / 60 + 0.1f) + "分钟");
		// 停止文件日志模块(！！！放到最后)
		// LogManager.shutdown();
		System.exit(0);
		return true;
	}

	/**
	 * 断线前逻辑
	 */
	public static void beforeShutDown() {
		GameMgr.server_status = ServerStatus.STOPPING;
		GameMgr.stopSave();
		GameMgr.unloadAll();
	}

	public static long getBeginSecs() {
		return beginSecs;
	}
}
