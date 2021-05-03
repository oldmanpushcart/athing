package com.github.ompc.athing.standard.platform.message;

import static com.github.ompc.athing.standard.platform.message.ThingMessage.Type.THING_STATE_CHANGED;

/**
 * 设备状态消息
 */
public class ThingStateMessage extends ThingMessage {

    private final State state;
    private final long lastOnlineTimestamp;
    private final String lastOnlineIp;

    /**
     * 设备状态消息
     *
     * @param productId           产品ID
     * @param thingId             设备ID
     * @param timestamp           消息时间戳
     * @param state               设备在线状态
     * @param lastOnlineTimestamp 最后上线时间戳
     * @param lastOnlineIp        最后上线IP地址
     */
    public ThingStateMessage(
            String productId, String thingId, long timestamp,
            State state, long lastOnlineTimestamp, String lastOnlineIp
    ) {
        super(THING_STATE_CHANGED, productId, thingId, timestamp);
        this.state = state;
        this.lastOnlineTimestamp = lastOnlineTimestamp;
        this.lastOnlineIp = lastOnlineIp;
    }

    /**
     * 获取设备在线状态
     *
     * @return 设备在线状态
     */
    public State getState() {
        return state;
    }

    /**
     * 获取最后上线时间戳
     *
     * @return 最后上线时间戳
     */
    public long getLastOnlineTimestamp() {
        return lastOnlineTimestamp;
    }

    /**
     * 获取最后上线IP地址
     *
     * @return 最后上线IP地址
     */
    public String getLastOnlineIp() {
        return lastOnlineIp;
    }

    /**
     * 设备状态
     */
    public enum State {

        /**
         * 设备在线
         */
        ONLINE,

        /**
         * 设备离线
         */
        OFFLINE

    }
}
