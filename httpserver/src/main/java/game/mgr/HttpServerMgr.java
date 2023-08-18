package game.mgr;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import game.entity.MergeDao;
import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.entity.UserInfoDao;
import game.entity.WhiteUserInfo;
import game.entity.WhiteUserInfoDao;
import game.entity.WxUsersSubscribeInfo;
import game.entity.WxUsersSubscribeInfoDao;
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

    /**
     * 白名单列表
     */
    private static Map<String, WhiteUserInfo> whiteUserInfoMap = new ConcurrentHashMap<>();

    /**
     * wx订阅名单
     */
    private static Map<String, WxUsersSubscribeInfo> wxUsersSubscribeMap = new ConcurrentHashMap<>();

    public static JSONObject getUserLoginInfo(String accountName) {
        JSONObject res = userLoginMap.get(accountName);
        if (res == null || res.isEmpty()) {
            res = UserInfoDao.getInstance().findUsersLoginInfo(accountName);
            if(!res.isEmpty()){
                userLoginMap.put(accountName, res);
            }
        }
        return res;
    }

    /**
     * 获得被合服的列表
     */
    public static List<Integer> getMergedServerIds(int targetServerId) {
        List<Integer> res = new ArrayList<>();
        for (ServerInfo info : serverMap.values()) {
            if (info.getTargetServerId() != targetServerId) {
                continue;
            }
            res.add(info.getServerId());
        }
        return res;
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

    public static boolean checkReload() {
        boolean needReload = MergeDao.getInstance().queryReload();
        if (needReload) {
            reload();
            MergeDao.getInstance().clearReload();
        }
        return needReload;
    }

    public static void reload() {
        serverMap.clear();
        whiteUserInfoMap.clear();
        wxUsersSubscribeMap.clear();
        userLoginMap.clear();
        //        gameServerConfig = null;
        init();
    }

    /**
     * 初始化数据
     */
    public static boolean init() {
        try {
            serverMap = ServerInfoDao.getInstance().findServerInfos();
            whiteUserInfoMap = WhiteUserInfoDao.getInstance().findWhiteUser();
            wxUsersSubscribeMap = WxUsersSubscribeInfoDao.getInstance().queryWxUsersSubscribeInfo();
            //            gameServerConfig = Files.fileToStr(new File("game_server.config"));
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

    public static Map<String, WhiteUserInfo> getWhiteUserInfoMap() {
        return whiteUserInfoMap;
    }

    public static Map<String, WxUsersSubscribeInfo> getWxUsersSubscribeInfoMap() {
        return wxUsersSubscribeMap;
    }

    public static void addUser2serverMap(String name, String playerName, int serverId, long loginTime, int level) {
        String sid = String.valueOf(serverId);
        JSONObject json = userLoginMap.get(name);
        if (json == null) {
            return;
        }
        JSONObject j = json.getJSONObject(sid);
        if (j == null) {
            j = new JSONObject();
            json.put(sid, j);
            return;
        }
        j.put("login_time", loginTime);
        j.put("player_name", playerName);
        j.put("level", level);
    }
}
