package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;

public class ThingReplyPromise<V> extends ThingTokenPromise<ThingReply<V>> implements ThingReplyFuture<V> {


    public ThingReplyPromise(Thing thing, String token) {
        super(thing, token);
    }

    public ThingReplyPromise(Thing thing, String token, Initializer<ThingReply<V>> initializer) {
        super(thing, token, initializer);
    }

}
