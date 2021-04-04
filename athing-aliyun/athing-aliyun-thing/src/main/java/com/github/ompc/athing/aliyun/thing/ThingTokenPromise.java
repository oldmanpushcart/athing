package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingTokenFuture;

/**
 * 设备令牌Promise
 *
 * @param <V> 类型
 */
public class ThingTokenPromise<V> extends ThingPromise<V> implements ThingTokenFuture<V> {

    private final String token;
    private final String _string;

    /**
     * 设备令牌Promise
     *
     * @param thing 设备
     * @param token 令牌
     */
    public ThingTokenPromise(Thing thing, String token) {
        this(thing, token, null);
    }

    /**
     * 设备令牌Promise
     *
     * @param thing       设备
     * @param token       令牌
     * @param initializer 初始化器
     */
    public ThingTokenPromise(Thing thing, String token, Initializer<V> initializer) {
        super(thing, initializer);
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
