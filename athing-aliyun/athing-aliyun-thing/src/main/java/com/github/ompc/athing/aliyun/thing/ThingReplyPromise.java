package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;

/**
 * 设备应答Promise
 *
 * @param <V> 类型
 */
public class ThingReplyPromise<V> extends ThingTokenPromise<ThingReply<V>> implements ThingReplyFuture<V> {

    private final String _string;

    /**
     * 设备应答Promise
     *
     * @param thing 设备
     * @param token 应答令牌
     */
    public ThingReplyPromise(Thing thing, String token) {
        this(thing, token, null);
    }

    /**
     * 设备应答Promise
     *
     * @param thing       设备
     * @param token       应答令牌
     * @param initializer 初始化器
     */
    public ThingReplyPromise(Thing thing, String token, Initializer<ThingReply<V>> initializer) {
        super(thing, token, initializer);
        this._string = String.format("%s?type=reply&token=%s", super.toString(), token);
    }

    @Override
    public String toString() {
        return _string;
    }

}
