package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.mgr.HttpServerMgr;

/**
 * @author tangjian
 * @date 2022-12-12 12:19
 * desc
 */
public class ReloadHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServerMgr.reload();
        JSONObject jsObject = new JSONObject();
        jsObject.put("status", "OK");

        //返回消息
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/reload".equals(target)) {
            return false;
        }
        return true;
    }
}