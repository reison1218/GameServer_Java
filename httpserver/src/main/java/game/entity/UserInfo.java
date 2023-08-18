package game.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户封装类
 *
 * @author reison
 */
public class UserInfo {

    public static UserInfo newInstance(String name, long combineId, int operatorId, int serverId,int level, String playerName,long loginTime) {
        UserInfo info = new UserInfo();
        info.setName(name);
        info.setCombineId(combineId);
        info.setOperatorId(operatorId);
        info.setServerId(serverId);
        info.setLevel(level);
        info.setPlayerName(playerName);
        info.setLastLoginTime(loginTime);
        return info;
    }

    private int id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private long combineId;

    @Getter
    @Setter
    private int operatorId;

    @Getter
    @Setter
    private int serverId;


    @Getter
    @Setter
    private String playerName;

    @Getter
    @Setter
    private long lastLoginTime;

    @Getter
    @Setter
    private int level;

    /**
     * 转换成object数组
     */
    public Object[] toObjectArray() {
        Object[] oj = new Object[7];
        oj[0] = name;
        oj[1] = combineId;
        oj[2] = operatorId;
        oj[3] = serverId;
        oj[4] = level;
        oj[5] = playerName;
        oj[6] = lastLoginTime;
        return oj;
    }

    @Override
    public String toString() {
        return "UserInfo{" + "id=" + id + ", name='" + name + '\'' + ", combineId=" + combineId + ", operatorId=" + operatorId + ", serverId=" +
                serverId + ", playerName='" + playerName + '\'' + '}';
    }
}
