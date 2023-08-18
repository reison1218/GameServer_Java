/* All rights reserved.This material is confidential and proprietary to HITALK team. */
package game.base.config;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import game.utils.HttpUtil;
import game.utils.JsonUtil;
import game.utils.Log;
import game.utils.RandomUtil;
import game.utils.StringUtils;

/**
 * <pre>
 * 配置工具类
 * </pre>
 *
 * @author reison
 * @time 2019年7月27日
 */
public final class Config {

    public static String CONF_DIR = "";// 配置文件所在目录
    public static String engName = "";// 服务器英文名！用于注册跨服连接，区分唯一性
    public static String site = "";// 服务器site值，用于UserInfo的site值
    public static String bigPlatformName = "";// 大区名！用于平台活动接口签名
    public static String showSite = ""; // S1、S2用于玩家头顶显示，如S1.随意玩玩
    public static String spName = ""; // 渠道英文名！用户UserInfo的渠道名
    public static String areaName = ""; // 区服中文名(选服列表显示的区服名)
    private final static boolean isTest = false;// 是否开启测试
    public static int areaid = 1; // 当前渠道xxx第几服
    public static int areaCt = 1; // 当前渠道已开服数量

    /**
     * config配置文件
     */
    private static JSONObject jsonMap = null;

    public final static boolean init() {
        return init("");
    }


    public final static boolean init(String configPost) {
        String appPath = System.getenv("PWD");
        if (SystemEnv.isProduction() && appPath == null) {
            Log.error("服务器启动错误，请从/xxx/xxx/app目录运行启动脚本！curPath:" + appPath);
            return false;
        }
        if (appPath != null) {
            CONF_DIR = appPath.replace("app", "config") + "/";
        }
        String configPath = "server.config";
        try {
            File file = null;
            if (SystemEnv.isProduction()) {
                configPath = CONF_DIR + "config/config" + configPost + ".json";
                file = new File(configPath);
            } else {
                file = new File(configPath);
            }
            if (!file.exists()) {
                Log.error(configPath + "配置文件不存在");
                return false;
            }

            String configStr = FileUtils.readFileToString(file, "UTF-8");
            jsonMap = (JSONObject) JsonUtil.parse(configStr);
            if (!jsonMap.containsKey(ConfigKey.MYSQL_CONF)) {
                Log.error(configPath + "中MySQL配置为空，请检查");
                return false;
            }
            //            if (!initNet()) {
            //            	return false;
            //            }
            // 打印配置文件
            Log.info(configPath + "配置文件：\n" + jsonMap.toJSONString());
        } catch (IOException e) {
            Log.error(configPath + "配置文件格式错误");
            return false;
        }
        return true;
    }

    /**
     * <pre>
     * 从登录服务器获取配置
     * </pre>
     */
    public final static boolean initNet() {
        try {
            // 必需参数检测
            String login_ip = jsonMap.getString("login_ip");
            int serverId = jsonMap.getIntValue("server_id");
            int login_port = jsonMap.getIntValue("login_port");
            if (serverId < 1) {
                Log.error("config.json缺失serverIdx配置");
                return false;
            }
            if (login_port < 1) {
                Log.error("config.json缺失login_port配置");
                return false;
            }
            if (login_ip == null || login_ip.isEmpty()) {
                Log.error("config.json缺失login_ip配置");
                return false;
            }
            // 获取配置
            String reqUrl = "http://" + login_ip + ":" + login_port + "/server/conf";
            Map<String, Object> params = new HashMap<>();
            params.put("auth", "NyxFlF1kmxt6Zx1L9Qbz2ciIDWYy9Fij");
            params.put("id", String.valueOf(serverId));
            JSONObject obj = (JSONObject) HttpUtil.doGetBackJson(reqUrl, params);
            if (obj.isEmpty() || obj.getIntValue("c") != 1) {
                Log.error("请求服务器配置为空或错误，请查看登录服日志,jsonObj：" + obj);
                return false;
            }
            String tempEngName = obj.getString("engname");
            areaid = obj.getIntValue("areaid");
            if (areaid == 0) {
                Log.error("登录服区服表，areaid字段没有值,id:" + serverId);
                return false;
            }
            if (StringUtils.isEmpty(tempEngName)) {
                Log.error("登录服区服表，engname字段没有值,id:" + serverId);
                return false;
            }
            if (!obj.containsKey(ConfigKey.GRP_CONF)) {
                Log.error("从登录服拿到的跨服分组配置为空,id:" + serverId);
                return false;
            }
            bigPlatformName = tempEngName;
            spName = obj.getString("spename");
            if (StringUtils.isEmpty(spName)) {
                spName = tempEngName;
            }
            areaCt = obj.getIntValue("ct");
            areaName = obj.getString("areaname");
            ConfigKey.tver = obj.getIntValue("tver");
            if (ConfigKey.tver < 1) {
                Log.error("请求登录服版本号错误,ver:" + ConfigKey.tver);
                return false;
            }

            showSite = "S" + areaid + ".";
            engName = tempEngName + ".s" + serverId;
            site = tempEngName + "_" + StringUtils.leftPad(serverId + "", '0', 5);
            jsonMap.putAll(obj);
        } catch (Exception e) {
            Log.error("请求服务器配置异常", e);
            return false;
        }
        Log.info("当前资源版本号：" + ConfigKey.tver);
        return true;
    }

    public static void main(String[] args) {

        //        try{
        //            File file = new File("server.config");
        //            String url = file.getAbsolutePath();
        //            BufferedReader in = new BufferedReader(new FileReader(url));
        //            StringBuffer sb = new StringBuffer();
        //            String str;
        //            while ((str = in.readLine()) != null) {
        //                sb.append(str);
        //            }
        //            System.out.println(sb.toString());
        //        }catch(Exception e){
        //            e.printStackTrace();
        //        }

        // 日志初始化
        Log.init(Config.class);

    }

    /**
     * <pre>
     * 获取游戏服配置
     * </pre>
     */
    public final static <T> T getConfig(String type, String key) {
        return getConfig(type, key, 0);
    }

    /**
     * <pre>
     * 获取第idx个配置数据
     * idx0：游戏服配置
     * idx1：战斗服配置
     * idx2：跨服配置
     * </pre>
     */
    public final static <T> T getConfig(String type, String key, Integer idx) {
        JSONArray redisArr = (JSONArray) jsonMap.get(type);
        if (redisArr == null || redisArr.isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked") Map<String, T> redisMap0 = (Map<String, T>) redisArr.get(idx);
        if (redisMap0 == null || redisMap0.isEmpty()) {
            Log.error("配置数组为空type：" + type);
            return null;
        }
        return redisMap0.get(key);
    }

    /**
     * <pre>
     * 获取某一配置所有字段
     * </pre>
     */
    public final static <T> Map<String, T> getConfig(String type, Integer idx) {
        JSONArray redisArr = (JSONArray) jsonMap.get(type);
        if (redisArr == null || redisArr.isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked") Map<String, T> redisMap0 = (Map<String, T>) redisArr.get(idx);
        if (redisMap0 == null || redisMap0.isEmpty()) {
            Log.error("配置数组为空type：" + type);
            return null;
        }
        return redisMap0;
    }

    /**
     * <pre>
     * 获取第一层配置
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public final static <T> T getConfig(String key) {
        return (T) jsonMap.get(key);
    }

    /**
     * <pre>
     * 获取第一层整型配置
     * </pre>
     */
    public final static int getIntConfig(String key) {
        return jsonMap.getIntValue(key);
    }

    /**
     * <pre>
     * 服务器英文名
     * ！用于注册跨服连接，区分唯一性
     * </pre>
     */
    public final static String getServerName() {
        return engName;
    }

    /**
     * <pre>
     * 服务器名
     * ！用于玩家头顶显示
     * </pre>
     */
    public final static String getShowSite() {
        return showSite;
    }

    /**
     * <pre>
     * 大区名！用于平台活动接口签名
     * </pre>
     */
    public static final String getBigPlatformName() {
        return bigPlatformName;
    }

    /**
     * <pre>
     * 渠道英文名！用户UserInfo的渠道名
     * </pre>
     */
    public static final String getSpName() {
        return spName;
    }

    /**
     * <pre>
     * 服务器site值，用于UserInfo的site值
     * </pre>
     */
    public static final String getSite() {
        return site;
    }

    public static final int getAreaid() {
        return areaid;
    }

    /**
     * <pre>
     * 在已开服范围中随机一个服
     * </pre>
     */
    public static final int randomAreaid() {
        int idxSt = 1, idxEd = areaCt + 1;
        if (idxEd < 2) {
            idxEd = 2;
        }
        return RandomUtil.rand(idxSt, idxEd);
    }

    /**
     * <pre>
     * 最大服id
     * </pre>
     */
    public static final int maxAreaid() {
        return areaCt;
    }

    public static final String getGroupConf(String groupConfType) {
        JSONObject jsonObj = jsonMap.getJSONObject(ConfigKey.GRP_CONF);
        int grpId = jsonObj.getIntValue(groupConfType);
        if (grpId < 1) {
            // Log.error("跨服活动分组配置为空，默认99999，type:" + groupConfType + ",jsonObj:" + jsonObj);
            grpId = 99999;// 新服默认值
        }
        return spName + "_" + grpId;
    }

    /**
     * <pre>
     * 更新活动分组
     * </pre>
     */
    public static final boolean updateGroupConf(JSONObject grpObj) {
        jsonMap.remove(ConfigKey.GRP_CONF);
        jsonMap.put(ConfigKey.GRP_CONF, grpObj);
        Log.info("更新jsonMap中的grpObj为" + grpObj);
        return true;
    }

    /**
     * <pre>
     * 是否开启测试
     * </pre>
     */
    public static final boolean isTest() {
        return isTest;
    }

    public static final String getAreaName() {
        return areaName;
    }
}
