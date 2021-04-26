package com.github.ompc.athing.aliyun.thing.runtime.messenger;

/**
 * 令牌数据
 */
public interface TokenData {

    /**
     * 生成数据
     *
     * @param token 令牌
     * @return 数据
     * @throws Exception 生成数据失败
     */
    Object make(String token) throws Exception;

}
