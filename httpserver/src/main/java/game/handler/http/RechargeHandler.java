package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.mgr.HttpServerMgr;
import game.utils.HttpUtil;
import game.utils.Log;
import game.utils.MD5;
import game.utils.StringUtils;

/**
 * @author tangjian
 * @date 2022-10-25 11:41
 * desc
 */
public class RechargeHandler extends BaseHandler {
    public static final int MAX_SERVER_ID = (1 << 14) - 1;

    public static final int MAX_OPERATOR_ID = (1 << 7) - 1;

    //充值回调游戏发货接口key
    public static final String recharge_callback_key = "b19cfd48a57b45d599c18a9dcf13b8f8";

    public static final String debug_recharge_callback_key = "12345678";

    public static final String RECHARGE_KEY = "(#hf9345870t3";

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            if (request.getParameterMap().isEmpty()) {
                Log.error("q1充值回调没有参数？！");
                return;
            }

            JSONObject params = new JSONObject();
            for (String name : request.getParameterMap().keySet()) {
                params.put(name, request.getParameter(name));
            }
            String order = params.getString("order");
            if (StringUtils.isEmpty(order)) {
                Log.error("q1充值参数没有order？！");
                return;
            }


            String sid = params.getString("sid");
            if (StringUtils.isEmpty(sid) || sid.equals("0")) {
                Log.error("q1充值参数没有sid？！order:" + order);
                return;
            }
            String actorid = params.getString("actorid");
            if (StringUtils.isEmpty(actorid)) {
                Log.error("q1充值回调没有actorid？！order:" + order);
                return;
            }
            if (!StringUtils.checkIsNum(actorid)) {
                Log.error("q1充值回调actorid不是数字！actorid：" + actorid + ",order:" + order);
                return;
            }

            if (StringUtils.isEmpty(params.getString("sdkorder"))) {
                Log.error("q1充值回调sdkorder是空的！order：" + order);
                return;
            }

            if (StringUtils.isEmpty(params.getString("user"))) {
                Log.error("q1充值回调user是空的！order：" + order);
                return;
            }

            if (StringUtils.isEmpty(params.getString("amount"))) {
                Log.error("q1充值回调amount是空的！order：" + order);
                return;
            }

            int serverId = safeParseInt(sid, 0);
            ServerInfo serverInfo = HttpServerMgr.getServerMap().get(serverId);
            if (serverInfo == null) {
                Log.error("服务器列表里面找不到id:" + serverId + "的数据？！");
                return;
            }

            JSONObject jsonParams = buildGameServerParams(params);
            String res = HttpUtil.doPost(serverInfo.getRechargeHttpUrl(), jsonParams);
            Log.info("充值回调，游戏服返回:" + res);
            sendResponse(res);
        } catch (Exception e) {
            Log.error("{}", e);
        }
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/q1/recharge".equals(target)) {
            return false;
        }
        return true;
    }

    public static int safeParseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }

        return defaultValue;
    }

    private static JSONObject buildGameServerParams(JSONObject params) {

        long actorId = Long.parseLong(params.getString("actorid"));
        int gamePid = getOperatorID(actorId);

        JSONObject res = new JSONObject();
        res.put("orderId", params.getString("sdkorder"));
        res.put("cpOrderId", params.getString("order"));
        res.put("userName", params.getString("user"));
        res.put("pid", String.valueOf(gamePid));
        res.put("sid", params.getString("sid"));
        res.put("amount", params.getString("amount"));
        res.put("sign", buildSign(res));
        res.put("channel", "android_q1");
        return res;
    }

    private static String buildSign(JSONObject params) {

        String orderId = params.getString("orderId");
        String userName = params.getString("userName");
        String pid = params.getString("pid");
        String sid = params.getString("sid");
        String amount = params.getString("amount");
        String cpOrderId = params.getString("cpOrderId");

        MD5 md5 = new MD5();
        md5.Update(orderId);
        md5.Update(cpOrderId);
        md5.Update(userName);
        md5.Update(pid);
        md5.Update(sid);
        md5.Update(amount);
        md5.Update(RECHARGE_KEY);
        return md5.asHex();
    }

    public static int getServerID(long combine) {
        return ((int) (combine >>> 32)) & MAX_SERVER_ID;
    }

    public static int getOperatorID(long combine) {
        return (int) (combine >>> 46);
    }
}

