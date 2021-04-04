package com.github.ompc.athing.standard.thing;

public interface ThingTokenFuture<V> extends ThingFuture<V> {

    /**
     * 获取令牌
     *
     * @return 令牌
     */
    String getToken();

}
