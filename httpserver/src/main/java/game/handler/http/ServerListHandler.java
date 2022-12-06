package game.handler.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;
import game.utils.StringUtils;


public class ServerListHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);

        String str = new String(bytes);

        JSONObject params = (JSONObject) JsonUtil.parse(str.trim());
        String accountName = params.getString("acc");
        String typeStr = params.getString("type");

        JSONObject jsObject = new JSONObject();

        //判定参数
        if (StringUtils.isEmpty(accountName)) {
            jsObject.put("result", "fail!");
            jsObject.put("errMessage", "the acc is null!");
            sendErrorResponse(503, jsObject.toJSONString());
            return;
        }

        if (StringUtils.isEmpty(typeStr)) {
            jsObject.put("result", "fail!");
            jsObject.put("errMessage", "the type is null!");
            sendErrorResponse(503, jsObject.toJSONString());
            return;
        }
        String[] values = typeStr.split(",");
        List<String> types = new ArrayList<>(Arrays.asList(values));

        JSONArray array = new JSONArray();
        long ctime = System.currentTimeMillis();
        long lastLoginTime;
        JSONObject userLoginJson = HttpServerMgr.getUserLoginInfo(accountName);
        String serverIdStr;
        for (ServerInfo si : HttpServerMgr.getServerMap().values()) {
            //是否可以显示
            if (!si.canShow(types, ctime)) {
                continue;
            }
            lastLoginTime = 0;

            serverIdStr = String.valueOf(si.getServerId());
            if (userLoginJson.containsKey(serverIdStr)) {
                lastLoginTime = userLoginJson.getLongValue(serverIdStr);
            }

            //如果这个服有角色就添加属性
            array.add(si.toJson(lastLoginTime));
        }
        jsObject.put("data", array);
        jsObject.put("status", "OK");
        //返回消息
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/server_list".equals(target)) {
            return false;
        }
        return true;
    }
}
