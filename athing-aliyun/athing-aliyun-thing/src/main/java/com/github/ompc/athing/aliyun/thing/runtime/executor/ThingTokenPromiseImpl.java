package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingTokenFuture;

import java.util.concurrent.Executor;

/**
 * 设备令牌承诺
 *
 * @param <V> 类型
 */
public class ThingTokenPromiseImpl<V> extends ThingPromiseImpl<V> implements ThingTokenFuture<V> {

    private final String token;
    private final String _string;

    /**
     * 设备令牌承诺
     *
     * @param token    令牌
     * @param thing    设备
     * @param executor 执行器
     */
    public ThingTokenPromiseImpl(String token, Thing thing, Executor executor) {
        super(thing, executor);
        this.token = token;
        this._string = String.format("%s?type=token&token=%s", super.toString(), token);
    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public String getToken() {
        return token;
    }

}
