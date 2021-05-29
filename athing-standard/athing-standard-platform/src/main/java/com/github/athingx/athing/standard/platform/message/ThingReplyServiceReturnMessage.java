package com.github.athingx.athing.standard.platform.message;

import com.github.athingx.athing.standard.component.Identifier;

import static com.github.athingx.athing.standard.platform.message.ThingMessage.Type.THING_REPLY_SERVICE_RETURN;

/**
 * 设备组件服务应答消息
 */
public class ThingReplyServiceReturnMessage extends ThingReplyMessage {

    private final Identifier identifier;
    private final Object data;

    /**
     * 设备服务应答消息
     *
     * @param productId  产品ID
     * @param thingId    设备ID
     * @param timestamp  消息时间戳
     * @param token      请求令牌
     * @param code       应答码
     * @param desc       应答描述
     * @param identifier 服务标识
     * @param data       服务数据
     */
    public ThingReplyServiceReturnMessage(
            String productId, String thingId, long timestamp,
            String token, int code, String desc,
            Identifier identifier, Object data
    ) {
        super(THING_REPLY_SERVICE_RETURN, productId, thingId, timestamp, token, code, desc);
        this.identifier = identifier;
        this.data = data;
    }

    /**
     * 获取服务标识
     *
     * @return 服务标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取服务返回结果
     *
     * @return 服务返回结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

}
