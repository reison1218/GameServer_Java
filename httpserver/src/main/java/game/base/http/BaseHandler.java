package game.base.http;

import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.utils.Log;
import lombok.Getter;

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


    @Getter
    Request baseRequest;

    @Getter
    HttpServletRequest request;

    @Getter
    HttpServletResponse response;

    public abstract void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public abstract void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public abstract boolean checkUrl(String target);


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!checkUrl(target)) {
            return;
        }
        this.baseRequest = baseRequest;
        this.request = request;
        this.response = response;
        Log.info("http-server收到请求,信息:" + request);
        try {
            response.setHeader("Content-Type", "text/html;charset=utf-8");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            if (request.getMethod().equals("GET")) {
                doGet(target, baseRequest, request, response);
            } else {
                doPost(target, baseRequest, request, response);
            }
            baseRequest.setHandled(true);
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            close();
        }
    }

    public void close() {
        try {
            if (request != null) {
                request.getInputStream().close();
                request = null;
            }
            if (response != null) {
                response.getOutputStream().close();
                response = null;
            }
            baseRequest = null;
        } catch (Exception e) {
            Log.error("{}", e);
        }
    }

    /**
     * 发送错误状态
     */
    public void sendErrorResponse(int code, String data) throws IOException {
        response.setStatus(code);
        if (data != null) {
            response.getOutputStream().write(data.trim().getBytes());
            response.getOutputStream().flush();
        }
        baseRequest.setHandled(true);
        close();
    }

    /**
     * 返回http请求
     */
    public void sendResponse(String data) throws IOException {
        if (response != null) {
            response.setStatus(SUCCESS);
        }
        if (response.getOutputStream() != null) {
            HttpOutput outputStream = (HttpOutput) response.getOutputStream();
            if (response != null && data != null && !outputStream.isClosed()) {
                outputStream.write(data.trim().getBytes());
                outputStream.flush();
            }
        }

        if (baseRequest != null) {
            baseRequest.setHandled(true);
        }
    }
}
