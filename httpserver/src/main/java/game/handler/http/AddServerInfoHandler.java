package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.Log;
import game.utils.StringUtils;

/**
 * @author tangjian
 * @date 2023-02-03 9:56
 * desc
 */
public class AddServerInfoHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //返回客户端消息
        JSONObject jsObject = new JSONObject();

        if (request.getParameterMap().isEmpty()) {
            Log.error("添加服务器没有参数？！");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "缺少参数！");
            sendResponse(jsObject.toJSONString());
            return;
        }

        String serverIdStr = request.getParameter("server_id");
        String name = request.getParameter("name");
        String ws = request.getParameter("ws");
        String openTime = request.getParameter("open_time");
        String registerState = request.getParameter("register_state");
        String mergeTimes = request.getParameter("merge_times");
        String type = request.getParameter("type");
        String manager = request.getParameter("manager");
        String inner_manager = request.getParameter("inner_manager");
        String serverType = request.getParameter("server_type");

        //校验serverId
        if (StringUtils.isEmpty(serverIdStr) || !StringUtils.checkIsNum(serverIdStr)) {
            Log.error("server_id不合法！");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "server_id不合法必须是数字");
            sendResponse(jsObject.toJSONString());
        }

        //校验name
        if (StringUtils.isEmpty(name)) {
            Log.error("name是空的");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "name不能为空");
            sendResponse(jsObject.toJSONString());
        }

        //校验ws
        if (StringUtils.isEmpty(ws)) {
            Log.error("ws是空的");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "ws不能为空");
            sendResponse(jsObject.toJSONString());
        }

        //校验openTime
        if (StringUtils.isEmpty(openTime)) {
            Log.error("openTime是空的");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "open_time不能为空");
            sendResponse(jsObject.toJSONString());
        }

        //校验registerState
        if (StringUtils.isEmpty(registerState)) {
            Log.error("registerState是空的");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "register_state不能为空");
            sendResponse(jsObject.toJSONString());
        }

        //校验mergeTimes
        if (StringUtils.isEmpty(mergeTimes) || !StringUtils.checkIsNum(mergeTimes)) {
            Log.error("mergeTimes不合法");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "merge_times不能为空,切必须是数字");
            sendResponse(jsObject.toJSONString());
        }

        //校验type
        if (StringUtils.isEmpty(type)) {
            Log.error("mergeTimes不合法");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "type不能为空");
            sendResponse(jsObject.toJSONString());
        }

        //校验rechargeHttpUrl
        if (StringUtils.isEmpty(manager)) {
            Log.error("manager不合法");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "manager不能为空");
            sendResponse(jsObject.toJSONString());
        }
        if(StringUtils.isEmpty(inner_manager)){
            Log.error("inner_manager不合法");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "innerManager不能为空");
            sendResponse(jsObject.toJSONString());
        }

        if (StringUtils.isEmpty(serverType)) {
            Log.error("serverType不合法");
            jsObject.put("status", "fail！");
            jsObject.put("mess", "server_type不能为空");
            sendResponse(jsObject.toJSONString());
        }

        int serverId = Integer.parseInt(serverIdStr);
        if (HttpServerMgr.getServerMap().containsKey(serverId)) {
            jsObject.put("status", "fail！");
            jsObject.put("mess", "该条记录已经存在！serverId:" + serverId);
        } else {
            ServerInfo si = new ServerInfo();
            si.setServerId(serverId);
            si.setName(name);
            si.setWs(ws);
            si.setOpenTime(openTime);
            si.setRegisterState(Integer.parseInt(registerState));
            si.setState(0);
            si.setLetter(0);
            si.setTargetServerId(0);
            si.setMergeTimes(Integer.parseInt(mergeTimes));
            si.setType(type);
            si.setManager(manager);
            si.setInnerManager(inner_manager);
            si.setServerType(Integer.parseInt(serverType));
            int res = ServerInfoDao.getInstance().insert(si);
            if (res > 0) {
                jsObject.put("status", "success！");
                HttpServerMgr.getServerMap().put(si.getServerId(), si);
            } else {
                jsObject.put("status", "fail！");
            }
        }
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/add_server".equals(target)) {
            return false;
        }
        return true;
    }
}