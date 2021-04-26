package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;

import java.util.concurrent.Executor;

/**
 * 设备应答承诺实现
 *
 * @param <V> 类型
 */
public class ThingReplyPromiseImpl<V> extends ThingTokenPromiseImpl<ThingReply<V>> implements ThingReplyFuture<V> {

    private final String _string;

    /**
     * 设备令牌承诺
     *
     * @param token    令牌
     * @param thing    设备
     * @param executor 执行器
     */
    public ThingReplyPromiseImpl(String token, Thing thing, Executor executor) {
        super(token, thing, executor);
        this._string = String.format("%s?type=reply&token=%s", super.toString(), token);
    }

    @Override
    public String toString() {
        return _string;
    }

}
