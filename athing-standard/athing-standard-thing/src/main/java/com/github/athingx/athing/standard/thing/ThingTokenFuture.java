package com.github.athingx.athing.standard.thing;

/**
 * 设备令牌凭证
 *
 * @param <V> 类型
 */
public interface ThingTokenFuture<V> extends ThingFuture<V> {

    /**
     * 获取令牌
     *
     * @return 令牌
     */
    String getToken();

}
