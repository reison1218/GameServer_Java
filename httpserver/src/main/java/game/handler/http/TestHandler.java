package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.utils.JsonUtil;
import game.utils.Log;

/**
 * @author tangjian
 * @date 2022-10-21 17:48
 * desc
 */
public class TestHandler extends BaseHandler {


    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
            Log.error("参数为null!");
            return;
        }
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean result = false;
        JSONObject jsObject = new JSONObject();
        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);
        String str = new String(bytes);
        JSONObject js = (JSONObject) JsonUtil.parse(str.trim());

        result = js.containsKey("game_id");
        if (!result) {
            sendErrorResponse(baseRequest,response,500,jsObject);
            Log.error("game_id param not find!");
            return;
        }

        int gameId = js.getInteger("game_id");

        if (gameId <= 0) {
            Log.error("game_id is invalid!");
            sendErrorResponse(baseRequest,response,500,jsObject);
            return;
        }
        sendResponse(baseRequest,response,jsObject);
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/test".equals(target)) {
            return false;
        }
        return true;
    }
}
