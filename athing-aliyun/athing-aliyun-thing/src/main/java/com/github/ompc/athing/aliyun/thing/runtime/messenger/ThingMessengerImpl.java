package com.github.ompc.athing.aliyun.thing.runtime.messenger;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingReplyPromiseImpl;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingTokenPromiseImpl;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqttMessage;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqttMessageImpl;
import com.github.ompc.athing.aliyun.thing.util.StringUtils;
import com.github.ompc.athing.standard.thing.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备信使实现
 */
public class ThingMessengerImpl implements ThingMessenger {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingBootOption option;
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingMqtt mqtt;
    private final Map<String, ThingPromise<?>> promises = new ConcurrentHashMap<>();
    private final Gson gson = GsonFactory.getGson();
    private final String _string;

    public ThingMessengerImpl(ThingBootOption option, Thing thing, ThingExecutor executor, ThingMqtt mqtt) {
        this.option = option;
        this.thing = thing;
        this.executor = executor;
        this.mqtt = mqtt;
        _string = String.format("%s/messenger", thing);
    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public ThingTokenFuture<Void> post(String topic, TokenData data) {
        final String token = StringUtils.generateToken();
        return executor.promise(new ThingTokenPromiseImpl<>(token, thing, executor), promise -> {

            // 构建MQTT消息
            final String payload = gson.toJson(data.make(token));
            final ThingMqttMessage message = new ThingMqttMessageImpl(payload.getBytes(UTF_8));

            // 发送MQTT消息
            promise.self()
                    .acceptDone(mqtt.publish(topic, message))
                    .onSuccess(future -> logger.debug("{} post message success, token={};topic={};message -> {}", this, token, topic, payload))
                    .onFailure(future -> logger.debug("{} post message failure, token={};topic={};message -> {}", this, token, topic, payload, future.getException()))
            ;

        });
    }

    @Override
    public ThingTokenFuture<Void> post(String topic, ThingReply<?> reply) {
        return executor.promise(new ThingTokenPromiseImpl<>(reply.getToken(), thing, executor), promise -> {

            // 构建MQTT消息
            final String payload = gson.toJson(reply);
            final ThingMqttMessage message = new ThingMqttMessageImpl(payload.getBytes(UTF_8));

            // 发送MQTT消息
            promise.self()
                    .acceptDone(mqtt.publish(topic, message))
                    .onSuccess(future -> logger.debug("{} post message success, token={};topic={};message -> {}", this, reply.getToken(), topic, payload))
                    .onFailure(future -> logger.debug("{} post message failure, token={};topic={};message -> {}", this, reply.getToken(), topic, payload, future.getException()))
            ;

        });
    }

    @Override
    public <T> ThingReplyFuture<T> call(String topic, TokenData data) {

        // TOKEN
        final String token = StringUtils.generateToken();

        return executor.promise(new ThingReplyPromiseImpl<>(token, thing, executor), promise -> {

            // 内容
            final Object content = data.make(token);

            // 构建MQTT消息
            final String payload = gson.toJson(content);
            final ThingMqttMessage message = new ThingMqttMessageImpl(payload.getBytes(UTF_8));

            // 开启超时任务
            final ThingFuture<Void> timerF = executor.submit(option.getReplyTimeoutMs(), TimeUnit.MILLISECONDS, () -> {
                if (null != promises.remove(token)) {
                    promise.tryException(new TimeoutException());
                }
            });

            // 注册promise
            promises.put(token, promise);

            // 承诺完成需要清理现场
            promise.onDone(future -> {
                timerF.cancel(true);
                promises.remove(token);
            });

            // 发送MQTT消息
            mqtt.publish(topic, message)
                    .onFailure(promise::acceptFail);

        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ThingPromise<T> reply(String token) {
        return (ThingPromise<T>) promises.remove(token);
    }

}
