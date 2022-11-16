package game;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import java.util.Set;

import game.base.config.Config;
import game.base.config.ConfigKey;
import game.base.config.ThreadCount.GateWayThreadCount;
import game.base.db.HikariDBPool;
import game.base.executor.ExecutorMgr;
import game.base.tcp.BaseNettyClient;
import game.base.tcp.SocketClient;
import game.base.tcp.SocketClientInitializer;
import game.handler.http.TestHandler;
import game.handler.tcp.SocketClientHandler;
import game.mgr.HttpServerMgr;
import game.mgr.TimeTaskMgr;
import game.utils.ClassUtil;
import game.utils.Log;
import io.netty.channel.Channel;

/**
 * @author tangjian
 * @date 2022-10-21 11:13
 * desc
 */
public class HttpServer {

    static Server rs = new Server();

    static BaseNettyClient socketClient = null;

    private static boolean needReconnSocketServer = false;

    /**
     * 开始启动时间
     */
    private static long beginSecs;

    /**
     * 开始函数
     */
    private static boolean start() {
        beginSecs = System.currentTimeMillis();

        // 来一发运行时挂掉钩子
        shutDownHook();

        if (!Log.init(HttpServer.class)) {
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
        // 初始化线程池
        if (!ExecutorMgr.init()) {
            return false;
        }
        // 初始化mgr
        if (!HttpServerMgr.init()) {
            return false;
        }
        // 定时器初始化
        if (!TimeTaskMgr.init()) {
            return false;
        }

        // 初始化jetty
        try {
            ServerConnector connector = new ServerConnector(rs);
            int port = Config.getConfig(ConfigKey.HTTP);
            connector.setPort(port);
            rs.setConnectors(new Connector[]{connector});
            HandlerList handlers = new HandlerList();

            Set<Class<?>> classSet = ClassUtil.getClasses(TestHandler.class.getPackage());
            Handler[] handlerArray = new Handler[classSet.size() + 1];
            int index = 0;
            for (Class cs : classSet) {
                handlerArray[index] = (Handler) cs.newInstance();
                index++;
            }
            handlerArray[handlerArray.length - 1] = new DefaultHandler();

            handlers.setHandlers(handlerArray);
            rs.setHandler(handlers);
            // 启动服务器
            rs.start();
            long endSecs = System.currentTimeMillis();
            long takeTime = endSecs - beginSecs;
            Log.info("用户中心启动完成，耗时：" + takeTime + "ms,监听端口：" + 8888);
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
                try {
                    Log.info(HttpServer.class.getSimpleName() + "进程钩子执行停服逻辑~");
                    stopServer(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

            }
        });
    }

    /**
     * 停服
     */
    public static boolean stopServer(boolean isShutdownHook) {
        beforeShutDown();
        HikariDBPool.stop();
        Log.info(HttpServer.class.getSimpleName() + "停止成功，本次运行：" +
                (int) Math.ceil((System.currentTimeMillis() / 1000 - getBeginSecs() / 1000) / 60 + 0.1f) + "分钟");
        if (isShutdownHook) {
            return true;
        }
        System.exit(0);
        return true;
    }

    /**
     * 断线前逻辑
     */
    public static void beforeShutDown() {
        HttpServerMgr.stop();
    }

    public static long getBeginSecs() {
        return beginSecs;
    }

    public static void main(String[] aa) {
        try {
            if (!start()) {
                Log.error("用户中心启动失败...");
                stopServer(false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            stopServer(false);
        }
    }

    /**
     * <pre>
     * 连接游戏服
     * </pre>
     */
    public static final boolean connectSocketServer() {
        String host = Config.getConfig(ConfigKey.SOCKET_SERVER);
        int port = Config.getIntConfig(ConfigKey.SOCKET_PORT);
        BaseNettyClient client = BaseNettyClient.createInstance(SocketClient.class, "SocketServer", host, port, GateWayThreadCount.GAME_CLIENT,
                new SocketClientInitializer(new SocketClientHandler()));
        if (client == null) {
            return false;
        }
        socketClient = client;
        if (socketClient.getChs().isEmpty() || !isActive(socketClient.getChs().get(0))) {
            Log.error("Socket服连接错误,host:" + host + ",port:" + port);
            return false;
        }
        Log.info("Socket服连接成功！");
        needReconnSocketServer = false;
        return true;
    }

    /**
     * <pre>
     * 连接是否有效
     * </pre>
     */
    private static final boolean isActive(Channel channel) {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public static boolean reconnSocketServer() {
        return socketClient.reconnect();
    }
}
