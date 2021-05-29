package com.github.athingx.athing.aliyun.platform;

/**
 * 设备平台连接密钥
 */
public class ThingPlatformAccess {

    private final String identity;
    private final String secret;

    /**
     * 设备平台连接密钥
     *
     * @param identity ACCESS_ID
     * @param secret   ACCESS_SECRET
     */
    public ThingPlatformAccess(String identity, String secret) {
        this.identity = identity;
        this.secret = secret;
    }

    public String getIdentity() {
        return identity;
    }

    public String getSecret() {
        return secret;
    }
}
