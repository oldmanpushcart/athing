package com.github.athingx.athing.aliyun.thing.runtime.caller;

import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;

/**
 * 设备方法访问返回（非异常），
 * 该异常主要用于控制物模型服务方法访问的返回方式，不要当成异常进行catch处理
 */
public class ThingCallReturnException extends RuntimeException {

    private final ThingPromise<?> promise;

    /**
     * 设备异步返回（异常）
     *
     * @param promise 返回承诺
     */
    public ThingCallReturnException(ThingPromise<?> promise) {
        this.promise = promise;
    }

    /**
     * 获取返回承诺
     *
     * @return 返回承诺
     */
    public ThingPromise<?> getPromise() {
        return promise;
    }

}
