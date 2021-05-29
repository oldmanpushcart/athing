package com.github.athingx.athing.aliyun.thing.runtime.messenger;

import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.standard.thing.ThingReply;
import com.github.athingx.athing.standard.thing.ThingReplyFuture;
import com.github.athingx.athing.standard.thing.ThingTokenFuture;

/**
 * 设备信使
 */
public interface ThingMessenger {

    /**
     * 投递
     *
     * @param serializer Json序列化器
     * @param topic      投递主题
     * @param data       令牌数据
     * @return 投递凭证
     */
    ThingTokenFuture<Void> post(JsonSerializer serializer, String topic, TokenData data);

    /**
     * 投递
     *
     * @param serializer Json序列化器
     * @param topic      投递主题
     * @param reply      投递数据
     * @return 投递凭证
     */
    ThingTokenFuture<Void> post(JsonSerializer serializer, String topic, ThingReply<?> reply);

    /**
     * 呼叫
     *
     * @param serializer Json序列化器
     * @param topic      呼叫主题
     * @param data       令牌数据
     * @param <T>        应答类型
     * @return 呼叫凭证
     */
    <T> ThingReplyFuture<T> call(JsonSerializer serializer, String topic, TokenData data);

    /**
     * 回复
     *
     * @param token 令牌
     * @param <T>   回复类型
     * @return 呼叫承诺，与{@link #call(JsonSerializer, String, TokenData)}的呼叫凭证对应
     */
    <T> ThingPromise<ThingReply<T>> reply(String token);

}
