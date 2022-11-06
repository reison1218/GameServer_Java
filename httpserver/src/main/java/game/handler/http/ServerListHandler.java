package game.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;
import game.utils.Log;


public class ServerListHandler extends AbstractHandler {
    /**
     * 成功的状态码
     **/
    public static int SUCCESS = 200;
    /**
     * 一天的秒数
     **/
    public static final int DAY_SEC = 604800;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/slg/server_list".equals(target)) {
            return;
        }
        Log.info("http-server收到服务器列表请求,信息:" + request);
        JSONObject jsObject = new JSONObject();
        try {
            response.setHeader("Content-Type", "text/html;charset=utf-8");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            if (request.getMethod().equals("GET")) {
                if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
                    Log.error("参数为null!");
                    return;
                }
            } else {
                JSONArray array = new JSONArray();
                for (ServerInfo si : HttpServerMgr.getServerMap().values()) {
                    array.add(si.toJson());
                }
                jsObject.put("data", array);
            }

            jsObject.put("status", "OK");
            response.setStatus(SUCCESS);
            response.setHeader("Content-Type", "text/html;charset=utf-8");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getOutputStream().write(jsObject.toJSONString().trim().getBytes());
            baseRequest.setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            request.getInputStream().close();
            response.getOutputStream().close();
        }
    }
}
