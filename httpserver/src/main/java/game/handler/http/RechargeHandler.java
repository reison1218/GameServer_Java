package game.handler;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.config.Config;
import game.base.http.BaseHandler;
import game.utils.JsonUtil;
import game.utils.Log;

/**
 * @author tangjian
 * @date 2022-10-25 11:41
 * desc
 */
public class RechargeHandler extends BaseHandler {



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

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);

        String str = new String(bytes);
        JSONObject params = (JSONObject) JsonUtil.parse(str.trim());

        //平台id
        String pid = params.getString("pid");
        //userid
        String user = params.getString("user");
        //订单号
        String order = params.getString("order");
        //sdk单号
        String sdkOrder = params.getString("sdkorder");
        //金额
        String amount = params.getString("amount");
        //?
        String sid = params.getString("sid");
        //?
        String actorid = params.getString("actorid");
        //?
        String productid = params.getString("productid");
        //?
        String currenttype = params.getString("currenttype");
        //?
        String checkproduct = params.getString("checkproduct");
        //?
        String bankcode = params.getString("bankcode");
        //?
        String developerPayload = params.getString("developerPayload");
        //签名
        String sign = params.getString("sign");

        //拼接字符串
        StringBuffer sb = new StringBuffer();
        sb.append(pid);
        sb.append("_");
        sb.append(user);
        sb.append("_");
        sb.append(order);
        sb.append("_");
        sb.append(sdkOrder);
        sb.append("_");
        sb.append(amount);
        sb.append("_");
        sb.append(sid);
        sb.append("_");
        sb.append(productid);
        sb.append("_");
        sb.append(checkproduct);
        sb.append("_");
        sb.append(actorid);
        sb.append("_");
        sb.append(Config.getConfig("debug")  ? debug_recharge_callback_key : recharge_callback_key);
    }

    @Override
    public boolean checkUrl(String target) {
        return false;
    }
}

