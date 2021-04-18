package com.github.ompc.athing.aliyun.thing.mqtt.paho;

import com.github.ompc.athing.aliyun.thing.ThingAccess;
import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttMessage;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttMessageHandler;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * 设备MQTT客户端实现（Paho）
 */
public class ThingMqttClientImplByPaho implements ThingMqttClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Thing thing;
    private final ThingExecutor executor;
    private final IMqttAsyncClient pahoClient;
    private final String _string;

    /**
     * 订阅三元组集合
     */
    private final Set<PahoSubTrip> trips = new LinkedHashSet<>();

    /**
     * 设备MQTT客户端实现（Paho）
     *
     * @param remote   远程地址
     * @param access   设备接入
     * @param option   启动选项
     * @param thing    设备
     * @param executor 设备执行器
     * @throws ThingException MQTT客户端构建异常
     */
    public ThingMqttClientImplByPaho(URI remote, ThingAccess access, ThingBootOption option, Thing thing, ThingExecutor executor) throws ThingException {
        this.thing = thing;
        this.executor = executor;
        this.pahoClient = new PahoClientFactory().make(remote, access, option);
        this._string = String.format("%s/client", thing);

        // 设置回调
        pahoClient.setCallback(new GroupPahoCallback(new ArrayList<MqttCallbackExtended>() {{

            // 主题自动订阅
            add(new AutoSubTripPahoCallback(null, pahoClient, trips));

            // 断线自动重连
            add(new AutoConnectPahoCallback(option, thing, null, pahoClient));

        }}));

    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public ThingFuture<Void> subscribe(String express, ThingMqttMessageHandler handler) {
        return new ThingPromise<>(thing, executor, promise -> {

            // 构造订阅三元组
            final PahoSubTrip trip = new PahoSubTrip(express, MQTT_QOS_EXACTLY_ONCE, new MqttMessageListenerImpl(handler));

            // 订阅后续动作
            promise.self()
                    .onSuccess(future -> trips.add(trip))
                    .onSuccess(future -> logger.debug("{} subscribe mqtt-message success! topic={};", ThingMqttClientImplByPaho.this, express))
                    .onFailure(future -> logger.debug("{} subscribe mqtt-message failure! topic={};", ThingMqttClientImplByPaho.this, express, future.getException()));

            // 如果已连接，则直接开始订阅
            if (isConnected()) {
                pahoClient.subscribe(trip.getExpress(), trip.getQos(), new Object(), new MqttActionListenerImpl(promise), trip.getListener());
            }

            // 如尚未连接，则加入到订阅三元组，等待连接后一次性订阅
            else {
                promise.trySuccess();
            }

        });

    }

    @Override
    public void syncSubscribe(String express, ThingMqttMessageHandler handler) throws ThingException {
        final ThingFuture<?> future = subscribe(express, handler).awaitUninterruptible();
        if (future.isFailure()) {
            throw new ThingException(thing, format("subscribe %s failure!", express), future.getException());
        }
    }

    @Override
    public ThingFuture<Void> publish(String topic, ThingMqttMessage message) {
        return new ThingPromise<>(thing, executor, promise -> {

            // 发布后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} publish mqtt-message success! topic={};", ThingMqttClientImplByPaho.this, topic))
                    .onFailure(future -> logger.debug("{} publish mqtt-message failure! topic={};", ThingMqttClientImplByPaho.this, topic, future.getException()));

            // 执行发布
            pahoClient.publish(topic, message.getData(), message.getQos(), false, new Object(), new MqttActionListenerImpl(promise));

        });

    }


    @Override
    public ThingFuture<Void> connect() {
        return new ThingPromise<>(thing, executor, promise -> {

            // 连接后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} connect success! remote={};", ThingMqttClientImplByPaho.this, pahoClient.getServerURI()))
                    .onFailure(future -> logger.debug("{} connect failure! remote={};", ThingMqttClientImplByPaho.this, pahoClient.getServerURI(), future.getException()));

            // 执行连接
            pahoClient.connect(new MqttConnectOptions(), new Object(), new MqttActionListenerImpl(promise));

        });

    }

    @Override
    public ThingFuture<Void> disconnect() {
        return new ThingPromise<>(thing, executor, promise -> {

            // 断开后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} disconnect success!", ThingMqttClientImplByPaho.this))
                    .onFailure(future -> logger.debug("{} disconnect failure!", ThingMqttClientImplByPaho.this, future.getException()));

            // 执行关闭
            pahoClient.disconnect(new Object(), new MqttActionListenerImpl(promise));

        });
    }

    @Override
    public boolean isConnected() {
        return pahoClient.isConnected();
    }

    /**
     * MQTT动作监听器
     */
    private static class MqttActionListenerImpl implements IMqttActionListener {

        private final ThingPromise<?> promise;

        private MqttActionListenerImpl(ThingPromise<?> promise) {
            this.promise = promise;
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            promise.trySuccess();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            promise.tryException(exception);
        }

    }

    /**
     * MQTT消息监听器
     */
    private class MqttMessageListenerImpl implements IMqttMessageListener {

        private final ThingMqttMessageHandler handler;

        private MqttMessageListenerImpl(ThingMqttMessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            new ThingPromise<MqttMessage>(thing, executor, promise -> {

                promise.self()
                        .onSuccess(future -> logger.debug("{} handle mqtt-message success! topic={};", ThingMqttClientImplByPaho.this, topic))
                        .onFailure(future -> logger.debug("{} handle mqtt-message failure! topic={};", ThingMqttClientImplByPaho.this, topic, future.getException()));

                handler.onMessage(topic, new ThingMqttMessage(message.getQos(), message.getPayload()));
                promise.trySuccess(message);

            });
        }

    }

}