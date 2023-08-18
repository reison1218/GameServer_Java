package game.entity;

import java.util.HashMap;
import java.util.Map;

import game.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangjian
 * @date 2023-07-26 15:45
 * desc
 */
public class WxUsersSubscribeInfo {

    public static final int UPDATE = 1;

    public static final int INSERT = 2;

    public static final int DELETE = 3;

    public boolean isUpdate() {
        return this.version == UPDATE;
    }

    public boolean isInsert() {
        return this.version == INSERT;
    }

    public boolean isDelete() {
        return this.version == DELETE;
    }

    public void setDelete() {
        this.version = DELETE;
    }

    public void setInsert() {
        this.version = INSERT;
    }

    public void setUpdate() {
        if (this.version == INSERT) {
            return;
        }
        this.version = UPDATE;
    }

    public void clearVersion() {
        this.version = 0;
    }

    //数据版本号
    private byte version;

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String openId;
    @Getter
    private Map<String, Integer> templIdMap = new HashMap<>();

    public void setTemplIdMap(String str) {
        if (StringUtils.isEmpty(str)) {
            return;
        }
        String[] strs = str.split(",");
        for (String id : strs) {
            String[] kv = id.split("\\|");
            templIdMap.put(kv[0], Integer.valueOf(kv[1]));
        }
    }

    public String getTemplIdMapStr() {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        for (String tempId : templIdMap.keySet()) {
            sb.append(tempId);
            sb.append("|");
            sb.append(templIdMap.get(tempId));
            if (index < templIdMap.size() - 1) {
                sb.append(",");
            }
            index++;
        }
        return sb.toString();
    }

    public void addTemplId(String key) {
        int v = 1;
        if (this.templIdMap.containsKey(key)) {
            v = this.templIdMap.get(key) + 1;
        }
        this.templIdMap.put(key, v);
        this.setUpdate();
    }
}
