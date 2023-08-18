package game.handler.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.http.BaseHandler;
import game.entity.ServerInfo;
import game.entity.ServerInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.JsonUtil;

/**
 * @author tangjian
 * @date 2023-01-31 11:12
 * desc
 */
public class MergeServerInfoHandler extends BaseHandler {

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = new byte[512];
        request.getInputStream().read(bytes);

        String str = new String(bytes);

        JSONObject params = (JSONObject) JsonUtil.parse(str.trim());
        JSONArray sourceJson = params.getJSONArray("source");
        JSONObject targetJson = params.getJSONObject("target");

        int targetId = targetJson.getIntValue("targetId");
        String targetName = targetJson.getString("targetName");

        //刷新target服
        refreshTarget(targetId, targetName);

        //刷新source服（将source的ws，target_server_id刷成最新的target数据）
        refreshSourceTarget(sourceJson, targetId);


        JSONObject jsObject = new JSONObject();
        jsObject.put("status", "OK");

        //返回消息
        sendResponse(jsObject.toJSONString());
    }

    private void refreshTarget(int targetId, String targetName) {
        ServerInfo si = HttpServerMgr.getServerMap().get(targetId);
        si.setName(targetName);
        ServerInfoDao.getInstance().update(si);
    }

    private void refreshSourceTarget(JSONArray sourceJson, int targetId) {

        ServerInfo targetServer = HttpServerMgr.getServerMap().get(targetId);
        //设置source
        for (int i = 0; i < sourceJson.size(); i++) {
            int sourceId = sourceJson.getIntValue(i);
            ServerInfo sourceServer = HttpServerMgr.getServerMap().get(sourceId);
            if (sourceServer == null) {
                continue;
            }
            sourceServer.setTargetServerId(targetId);
            sourceServer.setWs(targetServer.getWs());
            ServerInfoDao.getInstance().update(sourceServer);

            for (ServerInfo si : HttpServerMgr.getServerMap().values()) {
                if (si.getTargetServerId() != sourceId) {
                    continue;
                }
                si.setTargetServerId(targetId);
                si.setWs(targetServer.getWs());
                ServerInfoDao.getInstance().update(si);
            }
        }
    }


    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/merge_server".equals(target)) {
            return false;
        }
        return true;
    }
}