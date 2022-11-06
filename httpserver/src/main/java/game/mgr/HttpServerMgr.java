package game.mgr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.entity.UserInfo;
import game.entity.UserInfoDao;
import game.utils.Log;

/**
 * 用户中心mgr
 *
 * @author reison
 */
public class HttpServerMgr {

    /**
     * 玩家id初始值
     **/
    public static final int INIT_USEER_ID = 1000000;

    /**
     * 用户信息缓存队列,定时器持久化到数据库，延迟执行 key:gameId value:userId
     **/
    private static LinkedBlockingDeque<UserInfo> userQue = new LinkedBlockingDeque<UserInfo>();
    /**
     * 服务器列表信息
     **/
    private static Map<Integer, ServerInfo> serverMap = new HashMap<Integer, ServerInfo>();

    /**
     * 向队列头部塞入userinfo
     */
    public static void addUserInfo(UserInfo userInfo) {
        userQue.push(userInfo);
    }

    /**
     * <pre>
     * 保存数据
     * </pre>
     */
    public static int save() {
        int size = 0;
        while (!userQue.isEmpty()) {
            UserInfo userInfo = userQue.pollLast();
            if (userInfo == null) {
                Log.warn("userInfo is null!");
                continue;
            }
            UserInfoDao.getInstance().updateUserInfo(userInfo);
            size++;
        }
        return size;
    }

    /**
     * 停服
     */
    public static void stop() {
        int size = save();
        Log.info("停服执行完毕!save玩家size:" + size);
    }

    /**
     * 初始化数据
     */
    public static boolean init() {
        try {
            serverMap = ServerInfoDao.getInstance().findServerInfos();
        } catch (Exception e) {
            Log.error(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 根据游戏id获得服务器列表
     */
    public static ServerInfo getServerList(int serverId) {
        return serverMap.get(serverId);
    }

    public static Map<Integer, ServerInfo> getServerMap(){
        return serverMap;
    }
}
