package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.executor.Action;
import game.base.executor.ExecutorMgr;
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
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("res", "hello");
        sendResponse(jsonObject.toJSONString());
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
            sendErrorResponse(500, jsObject.toJSONString());
            Log.error("game_id param not find!");
            return;
        }

        int gameId = js.getInteger("game_id");

        if (gameId <= 0) {
            Log.error("game_id is invalid!");
            sendErrorResponse(500, jsObject.toJSONString());
            return;
        }
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/test".equals(target)) {
            return false;
        }
        return true;
    }
}
