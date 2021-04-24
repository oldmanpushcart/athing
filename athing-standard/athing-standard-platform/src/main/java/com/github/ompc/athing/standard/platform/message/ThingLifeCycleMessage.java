package com.github.ompc.athing.standard.platform.message;

import static com.github.ompc.athing.standard.platform.message.ThingMessage.Type.THING_LIFE_CYCLE;

/**
 * 设备生命周期消息
 */
public class ThingLifeCycleMessage extends ThingMessage {

    private final LifeCycle lifeCycle;

    /**
     * 设备消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param lifeCycle 生命周期
     */
    public ThingLifeCycleMessage(String productId, String thingId, long timestamp, LifeCycle lifeCycle) {
        super(THING_LIFE_CYCLE, productId, thingId, timestamp);
        this.lifeCycle = lifeCycle;
    }

    /**
     * 获取生命周期
     *
     * @return 生命周期
     */
    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    /**
     * 设备生命周期
     */
    public enum LifeCycle {

        /**
         * 设备创建
         */
        CREATE,

        /**
         * 设备删除
         */
        DELETE,

        /**
         * 设备启用
         */
        ENABLE,

        /**
         * 设备禁用
         */
        DISABLE

    }

}
