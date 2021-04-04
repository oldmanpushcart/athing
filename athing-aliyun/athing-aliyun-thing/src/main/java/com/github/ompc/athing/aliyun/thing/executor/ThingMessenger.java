package com.github.ompc.athing.aliyun.thing.executor;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Thing thing;
    private final ThingTimer timer;
    private final ThingConnectOption connOpt;
    private final IMqttAsyncClient client;
    private final Map<String, ThingPromise<?>> tokens = new ConcurrentHashMap<>();
    private final Gson gson = GsonFactory.getGson();


    /**
     * 设备信使
     *
     * @param thing   设备
     * @param timer   定时器
     * @param connOpt 连接参数
     * @param client  MQTT客户端
     */
    public ThingMessenger(Thing thing, ThingTimer timer, ThingConnectOption connOpt, IMqttAsyncClient client) {
        this.thing = thing;
        this.timer = timer;
        this.connOpt = connOpt;
        this.client = client;
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
        return new ThingPromise<>(thing, promise -> {

            // 构建MQTT消息
            final String payload = gson.toJson(message);
            final MqttMessage mqttMessage = new MqttMessage(payload.getBytes(UTF_8));
            mqttMessage.setRetained(false);
            mqttMessage.setQos(qos);

            // 发送MQTT消息
            client.publish(topic, mqttMessage, new Object(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    promise.trySuccess(null);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable cause) {
                    promise.tryException(cause);
                }
            });

            promise.self()
                    .onException(future -> logger.warn("{}/mqtt post message failure, topic={};qos={}; ", thing, topic, qos, future.getException()))
                    .onSuccess(future -> logger.debug("{}/mqtt post topic={};qos={}; message -> {}", thing, topic, qos, payload));

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
        return new ThingPromise<>(thing, promise -> {

            // 注册promise
            tokens.put(token, promise);

            // 开启超时任务
            final ThingFuture<Void> timerF = timer.after(connOpt.getReplyTimeoutMs(), TimeUnit.MILLISECONDS, () -> {
                if (null != tokens.remove(token)) {
                    promise.tryException(new TimeoutException());
                }
            });

            // 发送请求
            promise.self()
                    .acceptFailure(post(topic, MQTT_QOS_AT_LEAST_ONCE, message))
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

}
