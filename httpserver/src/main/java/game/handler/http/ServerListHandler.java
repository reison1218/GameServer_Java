package game.handler.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;


public class ServerListHandler extends BaseHandler {
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
        JSONObject jsObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (ServerInfo si : HttpServerMgr.getServerMap().values()) {
            array.add(si.toJson());
        }
        jsObject.put("data", array);
        jsObject.put("status", "OK");
        //返回消息
        sendResponse(baseRequest, response, jsObject);
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/server_list".equals(target)) {
            return false;
        }
        return true;
    }
}
