package game.mgr;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.entity.UserInfoDao;
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
    private static Map<Integer, ServerInfo> serverMap = new ConcurrentHashMap<>();

    public static final Map<String, JSONObject> userLoginMap = new ConcurrentHashMap<>();

    //key 玩家账号 登录的服务器id 时间
    public static final LoadingCache<String, JSONObject> userInfoLoader =
            CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterWrite(1, TimeUnit.DAYS).build(new HttpServerMgr.UserInfoLoader());

    static class UserInfoLoader extends CacheLoader<String, JSONObject> {
        @Override
        public JSONObject load(String s) throws Exception {
            JSONObject json = UserInfoDao.getInstance().findUsersLoginInfo(s);
            if (!json.isEmpty()) {
                userLoginMap.put(s, json);
            }
            return json;
        }
    }

    public static JSONObject getUserLoginInfo(String accountName) {
        //先看内存有么有
        JSONObject json = userLoginMap.get(accountName);
        if (json == null) {
            json = userInfoLoader.getUnchecked(accountName);
            if (json != null) {
                userLoginMap.put(accountName, json);
            }
        }
        return json;
    }

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

    public static void addUser2serverMap(String name, int serverId, long loginTime) {
        JSONObject json = userLoginMap.get(name);
        if (json == null) {
            json = new JSONObject();
            userLoginMap.put(name, json);
        }
        json.put(String.valueOf(serverId), loginTime);
    }
}
