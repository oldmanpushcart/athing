package com.github.ompc.athing.aliyun.thing.component.access;

/**
 * 设备接入
 */
public interface Access {

    /**
     * 获取产品ID
     *
     * @return 产品ID
     */
    String getProductId();

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    String getThingId();

    /**
     * 获取设备密码
     *
     * @return 设备密码
     */
    String getSecret();

}
