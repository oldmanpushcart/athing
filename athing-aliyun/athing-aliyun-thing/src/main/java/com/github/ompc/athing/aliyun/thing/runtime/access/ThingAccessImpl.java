package com.github.ompc.athing.aliyun.thing.runtime.access;

/**
 * 设备接入实现
 */
public class ThingAccessImpl implements ThingAccess {

    private final String productId;
    private final String thingId;
    private final String secret;

    public ThingAccessImpl(String productId, String thingId, String secret) {
        this.productId = productId;
        this.thingId = thingId;
        this.secret = secret;
    }

    @Override
    public String getProductId() {
        return productId;
    }

    @Override
    public String getThingId() {
        return thingId;
    }

    @Override
    public String getSecret() {
        return secret;
    }

}
