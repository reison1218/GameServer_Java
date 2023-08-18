package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.config.Config;
import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;
import game.utils.HttpUtil;
import game.utils.Log;

/**
 * @author tangjian
 * @date 2023-05-22 10:35
 * desc
 */
public class QuestionnaireHandler extends BaseHandler {
    public static int FAIL_STATE = -1;

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String serverIdStr = request.getParameter("serverid");
        if (StringUtils.isEmpty(serverIdStr)) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", FAIL_STATE);
            returnJson.put("msg", "找不到serverid参数");
            sendResponse(returnJson.toJSONString());
            return;
        }
        int serverId = Integer.parseInt(serverIdStr);
        ServerInfo info = HttpServerMgr.getServerMap().get(serverId);
        if (info == null) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", FAIL_STATE);
            returnJson.put("msg", "找不到ServerInfo! serverid:" + serverIdStr);
            sendResponse(returnJson.toJSONString());
            return;
        }
        String questUrl = info.getInnerManager() + "/api/common/questionnaire";
        Map<String, Object> params = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            params.put(key, request.getParameter(key));
        }
        String res = HttpUtil.doGet(questUrl, params);
        if (res == null) {
            Log.error("问卷调查-游戏服没有返回有效结果！" + request.toString());
        }
        sendResponse(res);
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serverIdStr = request.getParameter("serverid");
        if (StringUtils.isEmpty(serverIdStr)) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", FAIL_STATE);
            returnJson.put("msg", "找不到serverid参数");
            sendResponse(returnJson.toJSONString());
            return;
        }
        int serverId = Integer.parseInt(serverIdStr);
        ServerInfo info = HttpServerMgr.getServerMap().get(serverId);
        if (info == null) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", FAIL_STATE);
            returnJson.put("msg", "找不到ServerInfo! serverid:" + serverIdStr);
            sendResponse(returnJson.toJSONString());
            return;
        }

        String path = Config.getConfig("questionnaire_call_back_url");
        String questUrl = info.getInnerManager() + path;

        //封装url参数
        Map<String, Object> params = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            params.put(key, request.getParameter(key));
        }
        //封装body
        int len = request.getContentLength();
        String res;
        if (len <= 0) {
            res = HttpUtil.doGet(questUrl, params);
        } else {
            byte[] bytes = new byte[len];
            request.getInputStream().read(bytes);
            res = HttpUtil.doGet(questUrl, params,bytes);
        }

        if (res == null) {
            Log.error("问卷调查-游戏服没有返回有效结果！" + request.toString());
        }
        sendResponse(res);
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/q1/questionnaire".equals(target)) {
            return false;
        }
        return true;
    }
}