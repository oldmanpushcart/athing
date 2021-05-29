package com.github.athingx.athing.standard.platform.message;

/**
 * 设备组件属性设置应答消息
 */
public class ThingReplyPropertySetMessage extends ThingReplyMessage {

    /**
     * 设备属性设置应答消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param token     请求令牌
     * @param code      应答码
     * @param desc      应答描述
     */
    public ThingReplyPropertySetMessage(
            String productId, String thingId, long timestamp,
            String token, int code, String desc) {
        super(Type.THING_REPLY_PROPERTIES_SET, productId, thingId, timestamp, token, code, desc);
    }

}
