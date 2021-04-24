package com.github.ompc.athing.standard.platform.message;

import static com.github.ompc.athing.standard.platform.message.ThingMessage.Type.THING_REPLY_CONFIG_PUSH;

/**
 * 设备配置应答消息
 */
public class ThingReplyConfigPushMessage extends ThingReplyMessage {

    /**
     * 设备配置应答消息
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param reqId     请求ID
     * @param code      应答码
     * @param desc      应答描述
     */
    public ThingReplyConfigPushMessage(
            String productId, String thingId, long timestamp,
            String reqId, int code, String desc
    ) {
        super(THING_REPLY_CONFIG_PUSH, productId, thingId, timestamp, reqId, code, desc);
    }

}
