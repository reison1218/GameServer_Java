package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.UserInfo;
import game.entity.UserInfoDao;
import game.utils.JsonUtil;

/**
 * @author tangjian
 * @date 2022-11-14 9:57
 * desc
 */
public class SyncUserDataHandler extends BaseHandler {
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
        UserInfo info = UserInfo.newInstance(name, combineId, operatorId, serverId, playerName);

        UserInfoDao.getInstance().insertUserInfo(info);

        JSONObject jsObject = new JSONObject();
        jsObject.put("status", "success");
        //返回客户端消息
        sendResponse(baseRequest, response, jsObject);
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/sync_user".equals(target)) {
            return false;
        }
        return true;
    }
}
