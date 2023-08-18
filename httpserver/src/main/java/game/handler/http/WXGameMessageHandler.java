package game.handler.http;

import com.alibaba.fastjson.JSONObject;

import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import game.base.executor.ExecutorMgr;
import game.base.http.BaseHandler;
import game.entity.UserInfoDao;
import game.entity.WxUsersSubscribeInfo;
import game.entity.WxUsersSubscribeInfoDao;
import game.mgr.HttpServerMgr;
import game.utils.Log;
import game.utils.StringUtils;
import game.utils.WxSubscribeUtil;

/**
 * @author tangjian
 * @date 2023-05-24 10:57
 * desc 微信推送转发handler
 */
public class WXGameMessageHandler extends BaseHandler {

    String arena_fightTask_attackCity = "muyYTSpN35jEaRl_q3vNLMF9Job6vVyWF50fjvVY_RE";//  玩家订阅后晚上八点发送服务通知提醒玩家活动开始  擂台战/国战/金币异种入侵

    String auction = "TNNJufhO4WlmMl8hVxJMgkLOaTswHcbijcP6BuQhVBY";//神将拍卖提醒：有神将竞拍的日期首次登录时弹出订阅弹窗，玩家订阅后晚上18:50分发送服务通知提醒玩家神将拍卖即将结束


    String kingWar = "nTcGEUET4c03vnpVmLLqCtVzHvAi4fe3o4fRyEpLm00";//称王战提醒：称王战当天首次登录时弹出订阅弹窗，玩家订阅后在称王战的两个时间段12点和20点发送服务通知

    @Override
    public void doGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Log.info("收到wx_message请求！" + request.toString());
        String key = request.getParameter("key");
        String serverIdStr = request.getParameter("server_id");
        if (StringUtils.isEmpty(key)) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", -1);
            returnJson.put("msg", "找不到key参数");
            sendResponse(returnJson.toJSONString());
        }
        if (StringUtils.isEmpty(serverIdStr)) {
            JSONObject returnJson = new JSONObject();
            returnJson.put("code", -1);
            returnJson.put("msg", "找不到server_id参数");
            sendResponse(returnJson.toJSONString());
        }
        int serverId = Integer.parseInt(serverIdStr);
        String tempId = null;
        JSONObject j1;
        JSONObject j2;
        JSONObject j3;
        JSONObject data = new JSONObject();
        switch (key) {
            case "arena":
            case "fightTask":
            case "attackCity":
                tempId = arena_fightTask_attackCity;
                j1 = new JSONObject();
                j2 = new JSONObject();
                j3 = new JSONObject();
                if (key.equals("arena")) {
                    j1.put("value", "擂台战");
                    j2.put("value", "擂台币");
                } else if (key.equals("fightTask")) {
                    j1.put("value", "国战");
                    j2.put("value", "战功");
                } else if (key.equals("attackCity")) {
                    j1.put("value", "金币异种入侵");
                    j2.put("value", "金币");
                }
                j3.put("value", "20:00");
                data.put("thing1", j1);
                data.put("thing5", j2);
                data.put("time8", j3);
                break;

            case "auction":
                tempId = auction;
                j1 = new JSONObject();
                j1.put("value", "神将拍卖");
                j2 = new JSONObject();
                j2.put("value", "神将拍卖即将结束");
                data.put("thing1", j1);
                data.put("thing2", j2);
                break;
            case "kingWar":
                tempId = kingWar;

                j1 = new JSONObject();
                j1.put("value", "称王战开启");
                j2 = new JSONObject();
                j2.put("value", "称王之战已开启，请火速上线");
                data.put("thing1", j1);
                data.put("thing2", j2);
                break;
        }

        //拿到合到这个服务器的列表
        List<Integer> mergeList = HttpServerMgr.getMergedServerIds(serverId);
        mergeList.add(serverId);

        //拿到所有有这个服务器列表的账号
        Set<String> nameList = UserInfoDao.getInstance().queryNameByServerIds(mergeList);

        String finalTempId = tempId;
        ExecutorMgr.getUserSyncThreadPool().pushTask(1, () -> {
            try {
                StringBuffer sb = new StringBuffer();
                //查询账号，然后进行推送
                for (String name : nameList) {
                    WxUsersSubscribeInfo info = HttpServerMgr.getWxUsersSubscribeInfoMap().get(name);
                    if (info == null) {
                        continue;
                    }

                    //没有订阅这个模板的跳过
                    if (!info.getTemplIdMap().containsKey(finalTempId)) {
                        continue;
                    }

                    //订阅次数小于1的跳过
                    int v = info.getTemplIdMap().get(finalTempId);
                    if (v < 1) {
                        continue;
                    }
                    //推送微信
                    boolean res = WxSubscribeUtil.sendSubscribe(info.getOpenId(), finalTempId, data);
                    //次数-1
                    v -= 1;
                    info.getTemplIdMap().put(finalTempId, v);
                    WxUsersSubscribeInfoDao.getInstance().insertUserInfo(info);
                    if(res){
                        sb.append(info.getName() + "|");
                    }
                }
                if(StringUtils.isEmpty(sb.toString())){
                    return;
                }
                Log.info("触发微信推送 key：" + key + ",接受到的账号：" + sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void doPost(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

    @Override
    public boolean checkUrl(String target) {
        if (!"/slg/wx_message".equals(target)) {
            return false;
        }
        return true;
    }
}
