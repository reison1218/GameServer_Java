package game.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tangjian
 * @date 2022-10-25 11:17
 * desc
 */
public class RechargeInfo {

    @Getter@Setter
    private String orderId;

    @Getter@Setter
    private String operatorOrderId;

    @Getter@Setter
    long combineId;

    @Getter@Setter
    private int operatorId;

    @Getter@Setter
    private int serverId;

    @Getter@Setter
    private int userId;

    @Getter@Setter
    private String itemId;

    @Getter@Setter
    private int rmb;

    @Getter@Setter
    private int gold;

    @Getter@Setter
    private int time;

    @Getter@Setter
    private int processTime;

    @Getter@Setter
    private byte[] misc1;

    @Getter@Setter
    private byte[] misc2;

    @Override
    public String toString() {
        return "RechargeInfo{" + "orderId='" + orderId + '\'' + ", operatorOrderId='" + operatorOrderId + '\'' + ", combineId=" + combineId +
                ", operatorId=" + operatorId + ", serverId=" + serverId + ", userId=" + userId + ", itemId='" + itemId + '\'' + ", rmb=" + rmb +
                ", gold=" + gold + ", time=" + time + ", processTime=" + processTime + ", misc1=" + Arrays.toString(misc1) + ", misc2=" +
                Arrays.toString(misc2) + '}';
    }
}
