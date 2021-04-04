package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingTokenFuture;

public class ThingTokenPromise<V> extends ThingPromise<V> implements ThingTokenFuture<V> {

    private final String token;
    private final String _string;

    public ThingTokenPromise(Thing thing, String token) {
        this(thing, token, null);
    }

    public ThingTokenPromise(Thing thing, String token, Initializer<V> initializer) {
        super(thing, initializer);
        this.token = token;
        this._string = String.format("%s/promise/%s", thing, token);
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
