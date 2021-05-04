package com.github.ompc.athing.standard.platform.message;

/**
 * 设备上报消息
 */
public class ThingPostMessage extends ThingMessage {

    private final String token;

    /**
     * 设备上报消息
     *
     * @param type      消息类型
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param timestamp 消息时间戳
     * @param token     请求令牌
     */
    protected ThingPostMessage(
            Type type, String productId, String thingId, long timestamp,
            String token
    ) {
        super(type, productId, thingId, timestamp);
        this.token = token;
    }

    /**
     * 获取请求令牌
     *
     * @return 请求令牌
     */
    public String getToken() {
        return token;
    }

}
