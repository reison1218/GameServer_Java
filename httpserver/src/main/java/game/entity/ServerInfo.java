package game.entity;


import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import game.mgr.HttpServerMgr;
import game.utils.StringUtils;
import game.utils.TimeUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务器信息
 * 除了未到开服时间的不可见，其他都可见
 *
 * @author tangjian
 */
public class ServerInfo {

    /**
     * id
     **/
    private int serverId;
    /**
     * 游戏id
     **/
    private String name;

    private String ws;

    private String openTime;

    private int registerState;

    private int state;

    private int letter;

    private int targetServerId;

    private int mergeTimes;

    @Getter
    private List<String> type = new ArrayList<>();

    @Getter
    private String typeStr = null;

    @Setter
    @Getter
    private String manager;

    @Setter
    @Getter
    private String innerManager;

    @Setter
    @Getter
    private int serverType;

    public JSONObject toJson(long lastLoginTime) {
        JSONObject js = new JSONObject();
        js.put("server_id", this.serverId);
        js.put("name", this.name);
        js.put("ws_url", this.ws);
        js.put("open_time", this.getOpenTimeLong());
        js.put("register_state", this.registerState);
        js.put("state", this.state);
        js.put("letter", this.letter);
        js.put("target_server_id", this.targetServerId);
        js.put("merge_times", this.mergeTimes);
        js.put("type", this.type);
        js.put("last_login_time", lastLoginTime);
        js.put("questionnaire_http_url", getManager()+"/api/common/questionnaire");
        return js;
    }

    @Override
    public String toString() {
        return "ServerInfo{" + "serverId=" + serverId + ", name='" + name + '\'' + ", ws='" + ws + '\'' + ", openTime='" + openTime + '\'' +
                ", registerState=" + registerState + ", state=" + state + ", letter=" + letter + ", targetServerId=" + targetServerId +
                ", mergeTimes=" + mergeTimes + ", type=" + type + ", typeStr='" + typeStr + '\'' + ", manager='" + manager + '\'' + ", serverType=" +
                serverType +'}';
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWs() {
        return ws;
    }

    public void setWs(String ws) {
        this.ws = ws;
    }

    public String getOpenTime() {
        return openTime;
    }

    public long getOpenTimeLong() {
        Date date = TimeUtil.format(this.openTime);
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public int getRegisterState() {
        return registerState;
    }

    public void setRegisterState(int registerState) {
        this.registerState = registerState;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLetter() {
        return letter;
    }

    public void setLetter(int letter) {
        this.letter = letter;
    }

    public int getTargetServerId() {
        return targetServerId;
    }

    public void setTargetServerId(int targetServerId) {
        this.targetServerId = targetServerId;
    }

    public int getMergeTimes() {
        return mergeTimes;
    }

    public void setMergeTimes(int mergeTimes) {
        this.mergeTimes = mergeTimes;
    }


    public void setType(String type) {
        if (StringUtils.isEmpty(type)) {
            return;
        }
        typeStr = type;
        String[] types = type.split(",");
        for (String s : types) {
            this.type.add(s);
        }
    }

    public boolean canShow(String accName, List<String> sources, int serverType, long ctime) {
        //校验审核服
        if (this.serverType != serverType) {
            return false;
        }
        //如果是白名单全部显示
        boolean res = HttpServerMgr.getWhiteUserInfoMap().containsKey(accName);
        if (res) {
            return true;
        }
        boolean isContains = isContains(sources);
        if (!isContains) {
            return false;
        }
        long openTime = this.getOpenTimeLong();
        if (ctime < openTime) {
            return false;
        }
        return true;
    }

    private boolean isContains(List<String> sources) {
        for (String s : this.type) {
            if (sources.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
