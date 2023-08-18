package game.utils;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tangjian
 * @date 2023-07-21 9:57
 * desc
 */
public class WxSubscribeUtil {
    private static final Logger logger = LoggerFactory.getLogger(WxSubscribeUtil.class);
    public static String AppId = "wx4b4266b339f93ad5";

    public static String AppSecret = "20abd402973dd187460ea1d94591a9da";


    public static String sendSubscribeUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    public static String getAccessTokenUrl =
            "https://mp-weixin.q1.com/WXAPIService.asmx/GetAccessTokenS?appId=wx4b4266b339f93ad5&secret=20abd402973dd187460ea1d94591a9da";

    public static String getAccessToken() {
        String res = HttpUtil.doGet(getAccessTokenUrl, null);
        if (StringUtils.isEmpty(res)) {
            logger.error("获取微信接口调用凭证AccessToken失败！返回结果：{}", res);
            return null;
        }
        return res;
    }

    public static boolean sendSubscribe(String openId, String templateId, JSONObject data) {
        String accessToken = getAccessToken();
        if (StringUtils.isEmpty(accessToken)) {
            logger.warn("accessToken 获取返回是空的！");
            return false;
        }
        String url = sendSubscribeUrl + "?access_token=" + accessToken;
        JSONObject params = new JSONObject();
        params.put("touser", openId);
        params.put("template_id", templateId);
        //        params.put("page", "点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转");
        params.put("data", data);
        //        params.put("miniprogram_state", "跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版");
        params.put("miniprogram_state", "formal");
        JSONObject jsonRes = HttpUtil.doPostBackJson(url, params);
        if (jsonRes == null) {
            logger.info("微信返回结果是空的！");
            return false;
        }
        logger.info("微信返回：" + jsonRes.toJSONString());
        int res = jsonRes.getIntValue("errcode");
        if (res != 0) {
            return false;
        }
        return true;
    }
}
