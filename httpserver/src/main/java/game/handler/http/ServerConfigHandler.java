package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;

/**
 * @author tangjian
 * @date 2023-01-29 10:09
 * desc 服务器配置
 */
public class ServerConfigHandler extends BaseHandler {
    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        JSONObject jsObject = new JSONObject();
//        jsObject.put("server_config", HttpServerMgr.gameServerConfig);
//        //返回消息
//        sendResponse(jsObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/server_config".equals(target)) {
            return false;
        }
        return true;
    }
}
