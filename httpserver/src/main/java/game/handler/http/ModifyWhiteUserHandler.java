package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.WhiteUserInfo;
import game.entity.WhiteUserInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.Log;
import game.utils.StringUtils;

/**
 * @author tangjian
 * @date 2023-07-04 14:58
 * desc
 */
public class ModifyWhiteUserHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        //返回客户端消息
        JSONObject jsObject = new JSONObject();
        if (StringUtils.isEmpty(name)) {
            Log.error("name没有参数？！");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "缺少name参数！");
            sendResponse(jsObject.toJSONString());
            return;
        }

        //删掉
        if(!StringUtils.isEmpty(type) && type.equals("1")){
            HttpServerMgr.getWhiteUserInfoMap().remove(name);
            WhiteUserInfoDao.getInstance().deleteUserInfo(name);
        }else{
            //添加
            if (HttpServerMgr.getWhiteUserInfoMap().containsKey(name)) {
                jsObject.put("status", "success！");
                sendResponse(jsObject.toJSONString());
                return;
            }
            WhiteUserInfo wui = new WhiteUserInfo();
            wui.setName(name);
            WhiteUserInfoDao.getInstance().insertUserInfo(wui);
            HttpServerMgr.getWhiteUserInfoMap().put(name, wui);
        }
        jsObject.put("status", "success！");
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/modify_white_user".equals(target)) {
            return false;
        }
        return true;
    }
}
