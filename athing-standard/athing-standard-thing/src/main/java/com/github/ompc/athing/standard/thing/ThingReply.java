package com.github.ompc.athing.standard.thing;

/**
 * 设备应答
 */
public interface ThingReply<T> {

    /**
     * 是否应答成功
     *
     * @return TRUE|FALSE
     */
    boolean isOk();

    /**
     * 获取应答码
     *
     * @return 应答码
     */
    String getCode();

    /**
     * 获取应答信息
     *
     * @return 应答信息
     */
    String getMessage();

    /**
     * 获取应答数据
     *
     * @return 应答数据
     */
    T getData();

}
