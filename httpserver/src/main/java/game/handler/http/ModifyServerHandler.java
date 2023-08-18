package game.handler.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;

/**
 * @author tangjian
 * @date 2023-03-02 17:15
 * desc
 */
public class ModifyServerHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        JSONObject jsObject = new JSONObject();
        byte[] bytes = new byte[1024];

        try {
            request.getInputStream().read(bytes);
            String str = new String(bytes);
            JSONArray params = (JSONArray) JsonUtil.parse(str.trim());

            List<ServerInfo> infoList = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                JSONObject json = params.getJSONObject(i);
                if (!json.containsKey("server_id")) {
                    jsObject.put("status", "fail!");
                    jsObject.put("mess", "server_id不存在！");
                    break;
                }
                int serverId = json.getIntValue("server_id");
                String name = json.getString("name");
                String ws = json.getString("ws");
                String openTime = json.getString("open_time");
                int registerState = json.getIntValue("register_state");
                int state = json.getIntValue("state");
                int letter = json.getIntValue("letter");
                int targetServerId = json.getIntValue("target_server_id");
                int mergeTimes = json.getIntValue("merge_times");
                String type = json.getString("type");
                String manager = json.getString("manager");
                String inner_manager = json.getString("inner_manager");
                int serverType = json.getIntValue("server_type");

                ServerInfo info = HttpServerMgr.getServerMap().get(serverId);
                //新增
                if (info == null) {
                    jsObject.put("status", "fail!");
                    jsObject.put("mess", "数据不存在！server_id：" + serverId);
                    break;
                }

                if (name != null) {
                    info.setName(name);
                }
                if (ws != null) {
                    info.setWs(ws);
                }
                if (openTime != null) {
                    info.setOpenTime(openTime);
                }
                if (json.containsKey("register_state")) {
                    info.setRegisterState(registerState);
                }
                if (json.containsKey("state")) {
                    info.setState(state);
                }
                if (json.containsKey("letter")) {
                    info.setLetter(letter);
                }
                if (json.containsKey("target_server_id")) {
                    info.setTargetServerId(targetServerId);
                }
                if (json.containsKey("merge_times")) {
                    info.setMergeTimes(mergeTimes);
                }

                if (json.containsKey("server_type")) {
                    info.setServerType(serverType);
                }

                if (type!=null) {
                    info.setType(type);
                }
                if (manager != null) {
                    info.setManager(manager);
                }
                if(inner_manager!=null){
                    info.setInnerManager(inner_manager);
                }
                infoList.add(info);
            }

            for(ServerInfo info : infoList){
                ServerInfoDao.getInstance().update(info);
            }
            jsObject.put("status", "success!");
        } catch (Exception e) {
            jsObject.put("status", "fail!");
            jsObject.put("mess", "不是json array格式");
        }

        //返回消息
        sendResponse(jsObject.toJSONString());
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/modify_server".equals(target)) {
            return false;
        }
        return true;
    }
}