package com.github.athingx.athing.standard.platform.message;

/**
 * 设备消息
 */
public class ThingMessage {

    private final Type type;
    private final String productId;
    private final String thingId;
    private final long timestamp;

    /**
     * 设备消息
     *
     * @param type      消息类型
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     */
    protected ThingMessage(Type type, String productId, String thingId, long timestamp) {
        this.type = type;
        this.productId = productId;
        this.thingId = thingId;
        this.timestamp = timestamp;
    }

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 获取产品ID
     *
     * @return 产品ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getThingId() {
        return thingId;
    }

    /**
     * 获取消息时间戳
     *
     * @return 消息时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设备产品消息类型
     */
    public enum Type {

        /**
         * 服务异步返回应答
         */
        THING_REPLY_SERVICE_RETURN,

        /**
         * 属性设置返回应答
         */
        THING_REPLY_PROPERTIES_SET,

        /**
         * 设备配置推送应答
         */
        THING_REPLY_CONFIG_PUSH,

        /**
         * 设备报告事件
         */
        THING_POP_EVENT,

        /**
         * 设备报告属性
         */
        THING_POP_PROPERTIES,

        /**
         * 设备状态变更
         */
        THING_STATE_CHANGED,

        /**
         * 设备模块升级
         */
        THING_MODULAR_UPGRADE,

        /**
         * 设备模块报告
         */
        THING_MODULAR_REPORT,

        /**
         * 设备生命周期
         */
        THING_LIFE_CYCLE

    }

}
