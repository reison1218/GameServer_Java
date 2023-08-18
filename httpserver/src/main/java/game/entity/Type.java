package game.entity;

/**
 * @author tangjian
 * @date 2023-03-29 15:02
 * desc
 */
public enum Type {

    /**
     * 开发服
     */
    DEV(0),
    /**
     * 测试服
     */
    TEST(1),
    /**
     * 内测服
     */
    INNER_TEST(2),
    /**
     * 合服原服
     */
    MERGE_SOURCE(5),
    /**
     * 正式服
     */
    OFFICIAL(10);

    Type(int i) {
    }
}
