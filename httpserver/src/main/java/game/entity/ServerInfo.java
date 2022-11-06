package game.entity;


import com.alibaba.fastjson.JSONObject;

import game.utils.TimeUtil;

/**
 * 服务器信息
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

    private String type;

    public JSONObject toJson(){
        JSONObject js = new JSONObject();
        js.put("server_id",this.serverId);
        js.put("name",this.name);
        js.put("ws_url",this.ws);
        js.put("open_time",this.getOpenTimeLong());
        js.put("register_state",this.registerState);
        js.put("state",this.state);
        js.put("letter",this.letter);
        js.put("target_server_id",this.targetServerId);
        js.put("merge_times",this.mergeTimes);
        js.put("type",this.type);
        return js;
    }

    @Override
    public String toString() {
        return "ServerInfo{" + "serverId=" + serverId + ", name='" + name + '\'' + ", ws='" + ws + '\'' + ", openTime='" + openTime + '\'' +
                ", registerState=" + registerState + ", state=" + state + ", letter=" + letter + ", targetServerId=" + targetServerId +
                ", mergeTimes=" + mergeTimes + ", type=" + type + '}';
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

    public long getOpenTimeLong(){
        return TimeUtil.format(this.openTime).getTime();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
