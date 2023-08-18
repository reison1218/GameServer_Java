package game.handler.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.executor.ExecutorMgr;
import game.base.http.BaseHandler;
import game.entity.WxUsersSubscribeInfo;
import game.entity.WxUsersSubscribeInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;
import game.utils.Log;
import game.utils.StringUtils;

/**
 * @author tangjian
 * @date 2023-07-25 15:26
 * desc
 */
public class WXUserSubscribeHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);

        String str = new String(bytes);

        JSONObject params = (JSONObject) JsonUtil.parse(str.trim());
        JSONObject jsonObject = new JSONObject();
        String account = params.getString("account");
        String openId = params.getString("open_id");
        JSONArray templIds = params.getJSONArray("templIds");

        if (StringUtils.isEmpty(openId)) {
            Log.warn("微信订阅，open_id是空的？");
            jsonObject.put("result", "fail!");
            jsonObject.put("errMessage", "微信订阅，open_id是空的？");
            return;
        }
        if (StringUtils.isEmpty(account)) {
            Log.warn("微信订阅，account是空的？");
            jsonObject.put("result", "fail!");
            jsonObject.put("errMessage", "微信订阅，account是空的？");
            return;
        }
        if (templIds == null || templIds.isEmpty()) {
            Log.warn("微信订阅，templIds是空的？");
            jsonObject.put("result", "fail!");
            jsonObject.put("errMessage", "微信订阅，templIds是空的？");
            return;
        }

        ExecutorMgr.getUserSyncThreadPool().pushTask(1, () -> {
            try {
                WxUsersSubscribeInfo wx = HttpServerMgr.getWxUsersSubscribeInfoMap().get(account);
                if (wx == null) {
                    wx = new WxUsersSubscribeInfo();
                    wx.setName(account);
                    HttpServerMgr.getWxUsersSubscribeInfoMap().put(wx.getName(), wx);
                }
                wx.setOpenId(openId);
                for (int i = 0; i < templIds.size(); i++) {
                    wx.addTemplId(templIds.getString(i));
                }
                WxUsersSubscribeInfoDao.getInstance().insertUserInfo(wx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Log.info("收到用户wx订阅请求："+params.toJSONString());
        //返回消息
        jsonObject.put("result", "OK");
        jsonObject.put("errMessage", "success");
        sendResponse(jsonObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/wx_subscribe".equals(target)) {
            return false;
        }
        return true;
    }
}
