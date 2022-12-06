package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.executor.ExecutorMgr;
import game.base.http.BaseHandler;
import game.entity.UserInfo;
import game.entity.UserInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;
import game.utils.Log;

/**
 * @author tangjian
 * @date 2022-11-14 9:57
 * desc
 */
public class SyncUserDataHandler extends BaseHandler {

    public static final int SYNC_HTTP_TYPE_CREATE = 1;

    public static final int SYNC_HTTP_TYPE_LOGIN = 2;

    public static final int SYNC_HTTP_TYPE_NAME = 3;
    /**
     * 成功的状态码
     **/
    public static int SUCCESS = 200;
    /**
     * 一天的秒数
     **/
    public static final int DAY_SEC = 604800;

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);
        String str = new String(bytes);
        JSONObject params = (JSONObject) JsonUtil.parse(str.trim());
        String name = params.getString("name");
        long combineId = params.getLong("combine_id");
        int operatorId = params.getIntValue("operator_id");
        int serverId = params.getIntValue("server_id");
        String playerName = params.getString("player_name");
        long loginTime = params.getLongValue("login_time");
        ExecutorMgr.getUserSyncThreadPool().pushTask(1, () -> {
            try {
                //持久化数据库
                UserInfo info = UserInfo.newInstance(name, combineId, operatorId, serverId, playerName, loginTime);
                UserInfoDao.getInstance().insertUserInfo(info);
                //同步撸到内存
                HttpServerMgr.addUser2serverMap(name, serverId, loginTime);

            } catch (Exception e) {
                Log.error("{}", e);
            }
        });
        //返回客户端消息
        JSONObject jsObject = new JSONObject();
        jsObject.put("status", "success");
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/sync_user".equals(target)) {
            return false;
        }
        return true;
    }
}
