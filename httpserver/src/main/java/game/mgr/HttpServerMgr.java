package game.mgr;

import java.util.HashMap;
import java.util.Map;

import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.utils.Log;

/**
 * 用户中心mgr
 *
 * @author reison
 */
public class HttpServerMgr {

    /**
     * 服务器列表信息
     **/
    private static Map<Integer, ServerInfo> serverMap = new HashMap<Integer, ServerInfo>();


    /**
     * 停服
     */
    public static void stop() {
        Log.info("停服执行完毕!");
    }

    /**
     * 停服
     */
    public static int save() {
        Log.info("保存完毕!");
        return 0;
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

    public static Map<Integer, ServerInfo> getServerMap() {
        return serverMap;
    }
}
