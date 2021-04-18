package com.github.ompc.athing.aliyun.thing;

import java.util.Objects;

/**
 * 设备接入
 */
public class ThingAccess {

    private final String productId;
    private final String thingId;
    private final String secret;

    /**
     * 设备连接密钥
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @param secret    设备密码
     */
    public ThingAccess(String productId, String thingId, String secret) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.thingId = Objects.requireNonNull(thingId, "thingId");
        this.secret = Objects.requireNonNull(secret, "secret");
    }

    public String getProductId() {
        return productId;
    }

    public String getThingId() {
        return thingId;
    }

    public String getSecret() {
        return secret;
    }

}
