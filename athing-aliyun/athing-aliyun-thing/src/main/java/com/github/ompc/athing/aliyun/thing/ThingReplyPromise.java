package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;

import java.util.concurrent.Executor;

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
     * @param thing    设备
     * @param token    令牌
     * @param executor 通知执行线程池
     */
    public ThingReplyPromise(Thing thing, String token, Executor executor) {
        super(thing, token, executor);
        this._string = String.format("%s?type=reply&token=%s", super.toString(), token);
    }

    @Override
    public String toString() {
        return _string;
    }

}
