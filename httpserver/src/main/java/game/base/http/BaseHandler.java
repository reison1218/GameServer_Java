package game.base.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.utils.Log;

/**
 * @author tangjian
 * @date 2022-10-25 15:25
 * desc
 */
public abstract class BaseHandler extends AbstractHandler {

    //充值回调游戏发货接口key
    public static final String recharge_callback_key = "b19cfd48a57b45d599c18a9dcf13b8f8";

    public static final String debug_recharge_callback_key = "12345678";
    /**
     * 成功的状态码
     **/
    public static int SUCCESS = 200;

    public static int FAIL = -3;
    /**
     * 一天的秒数
     **/
    public static final int DAY_SEC = 604800;

    public abstract void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public abstract void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public abstract boolean checkUrl(String target);


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!checkUrl(target)) {
            return;
        }
        Log.info("http-server收到充值请求,信息:" + request);
        try {
            response.setHeader("Content-Type", "text/html;charset=utf-8");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            if (request.getMethod().equals("GET")) {
                doGet(target, baseRequest, request, response);
            } else {
                doPost(target, baseRequest, request, response);
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            request.getInputStream().close();
            response.getOutputStream().close();
        }
    }

    /**
     * 发送错误状态
     */
    public void sendErrorResponse(Request baseRequest, HttpServletResponse response, int code) throws IOException {
        response.setStatus(SUCCESS);
        response.setHeader("Content-Type", "text/html;charset=utf-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getOutputStream().write(code);
        baseRequest.setHandled(true);
    }

    /**
     * 返回http请求
     */
    public void sendResponse(Request baseRequest, HttpServletResponse response, JSONObject dataJson) throws IOException {
        response.setStatus(SUCCESS);
        response.setHeader("Content-Type", "text/html;charset=utf-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getOutputStream().write(dataJson.toJSONString().trim().getBytes());
        baseRequest.setHandled(true);
    }

}
