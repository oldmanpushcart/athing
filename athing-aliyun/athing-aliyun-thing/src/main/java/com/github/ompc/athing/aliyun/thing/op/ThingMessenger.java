package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttMessage;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备信使
 */
public class ThingMessenger {

    public static final int MQTT_QOS_AT_MOST_ONCE = 0;
    public static final int MQTT_QOS_AT_LEAST_ONCE = 1;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingBootOption option;
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingTimer timer;
    private final ThingMqttClient client;
    private final Map<String, ThingPromise<?>> tokens = new ConcurrentHashMap<>();
    private final Gson gson = GsonFactory.getGson();
    private final String _string;


    /**
     * 设备信使
     *
     * @param thing  设备
     * @param option 连接参数
     * @param client 设备MQTT客户端
     * @param timer  设备定时器
     */
    public ThingMessenger(ThingBootOption option, Thing thing, ThingExecutor executor, ThingMqttClient client, ThingTimer timer) {
        this.thing = thing;
        this.executor = executor;
        this.timer = timer;
        this.option = option;
        this.client = client;
        this._string = String.format("%s/messenger", thing);
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 投递消息
     *
     * @param topic   消息主题
     * @param message 消息
     * @return 投递Future
     */
    public ThingFuture<Void> post(String topic, Object message) {
        return post(topic, MQTT_QOS_AT_MOST_ONCE, message);
    }

    /**
     * 投递消息
     *
     * @param topic   消息主题
     * @param qos     投递QOS
     * @param message 消息
     * @return 投递Future
     */
    public ThingFuture<Void> post(String topic, int qos, Object message) {

        return new ThingPromise<>(thing, executor, promise -> {

            // 构建MQTT消息
            final String payload = gson.toJson(message);

            // 发送MQTT消息
            promise.acceptDone(
                    client.publish(topic, new ThingMqttMessage(payload.getBytes(UTF_8)))
                            .onSuccess(future -> logger.debug("{} post message success, topic={};qos={}; message -> {}", ThingMessenger.this, topic, qos, payload))
                            .onFailure(future -> logger.debug("{} post message failure, topic={};qos={}; ", ThingMessenger.this, topic, qos, future.getException()))
            );

        });

    }


    /**
     * 请求呼叫
     *
     * @param token   令牌
     * @param topic   请求主题
     * @param message 请求内容
     * @param <V>     应答数据类型
     * @return 呼叫Future
     */
    public <V> ThingFuture<V> call(String token, String topic, Object message) {
        return new ThingPromise<>(thing, executor, promise -> {

            // 注册promise
            tokens.put(token, promise);

            // 开启超时任务
            final ThingFuture<Void> timerF = timer.after(option.getReplyTimeoutMs(), TimeUnit.MILLISECONDS, () -> {
                if (null != tokens.remove(token)) {
                    promise.tryException(new TimeoutException());
                }
            });

            // 发送请求
            promise.self()
                    .acceptFail(post(topic, MQTT_QOS_AT_LEAST_ONCE, message))
                    .onDone(future -> {
                        timerF.cancel(true);
                        tokens.remove(token);
                    });

        });


    }

    /**
     * 应答呼叫
     *
     * @param token 令牌
     * @param <V>   应答数据类型
     * @return 呼叫Future
     */
    @SuppressWarnings("unchecked")
    public <V> ThingPromise<V> reply(String token) {
        return (ThingPromise<V>) tokens.remove(token);
    }

    /**
     * 销毁
     */
    public void destroy() {
        new HashMap<>(tokens).forEach((token, promise) -> {
            if (promise.tryCancel()) {
                logger.info("{}/call/{} is cancelled by destroy!", this, token);
            }
        });
        logger.info("{} is destroyed!", this);
    }

}
