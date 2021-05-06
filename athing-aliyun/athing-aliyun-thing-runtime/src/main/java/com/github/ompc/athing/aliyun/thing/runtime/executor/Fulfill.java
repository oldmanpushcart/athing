package com.github.ompc.athing.aliyun.thing.runtime.executor;

/**
 * 承诺履约
 *
 * @param <V> 类型
 */
@FunctionalInterface
public interface Fulfill<V> {

    /**
     * 履约承诺
     *
     * @param promise 设备承诺
     * @throws Throwable 履约失败
     */
    void fulfilling(ThingPromise<V> promise) throws Throwable;

}
