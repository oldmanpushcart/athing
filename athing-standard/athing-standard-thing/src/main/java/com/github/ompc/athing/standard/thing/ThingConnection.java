package com.github.ompc.athing.standard.thing;

/**
 * 设备连接
 */
public interface ThingConnection {

    /**
     * 断开操作
     *
     * @return 操作存根
     */
    ThingFuture<Void> disconnect();

    /**
     * 获取断开存根
     *
     * @return 断开存根
     */
    ThingFuture<Void> getDisconnectFuture();

}
