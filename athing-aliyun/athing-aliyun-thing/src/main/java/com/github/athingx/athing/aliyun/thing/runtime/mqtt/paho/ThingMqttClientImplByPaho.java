package com.github.athingx.athing.aliyun.thing.runtime.mqtt.paho;

import com.github.athingx.athing.aliyun.thing.ThingBootOption;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.*;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.*;
import com.github.athingx.athing.aliyun.thing.util.ThingFutureUtils;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.ThingFuture;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttMessage.MQTT_QOS_EXACTLY_ONCE;
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
     * 关闭承诺
     */
    private volatile ThingPromise<Void> disconnectP;
    /**
     * 销毁标记
     */
    private volatile boolean isDestroyed = false;

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
        this._string = String.format("%s/mqtt", thing);

        // 设置回调
        pahoClient.setCallback(new GroupPahoCallback(new ArrayList<MqttCallbackExtended>() {{

            // 主题自动订阅
            add(new AutoSubTripPahoCallback(ThingMqttClientImplByPaho.this, pahoClient, trips));

            // 断线手动重连
            add(new PahoCallbackAdapter() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("{} is lost connection!", ThingMqttClientImplByPaho.this, cause);
                    synchronized (ThingMqttClientImplByPaho.this) {
                        if (null != disconnectP) {
                            disconnectP.trySuccess();
                        }
                    }
                }
            });


        }}));

    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public ThingFuture<Void> subscribe(String express, ThingMqttMessageHandler handler) {
        return executor.promise(promise -> {

            // 构造订阅三元组
            final PahoSubTrip trip = new PahoSubTrip(express, MQTT_QOS_EXACTLY_ONCE, new MqttMessageListenerImpl(handler));

            // 订阅后续动作
            promise.self()
                    .onSuccess(future -> trips.add(trip))
                    .onSuccess(future -> logger.debug("{} subscribe success! topic={};", this, express))
                    .onFailure(future -> logger.debug("{} subscribe failure! topic={};", this, express, future.getException()));

            // 如果已连接，则直接开始订阅
            if (_isConnected()) {
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
            throw new ThingException(thing, format("subscribe: %s failure!", express), future.getException());
        }
    }

    @Override
    public ThingFuture<Void> publish(String topic, ThingMqttMessage message) {
        return executor.promise(promise -> {

            // 发布后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} publish success! >> topic={};", this, topic))
                    .onFailure(future -> logger.debug("{} publish failure! >> topic={};", this, topic, future.getException()));

            // 执行发布
            pahoClient.publish(topic, message.getData(), message.getQos(), false, new Object(), new MqttActionListenerImpl(promise));

        });

    }


    private ThingFuture<Void> _connect() {
        return executor.promise(promise -> {

            // 连接后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} connect success! remote={};", this, pahoClient.getServerURI()))
                    .onFailure(future -> logger.debug("{} connect failure! remote={};", this, pahoClient.getServerURI(), future.getException()));

            // 执行连接
            pahoClient.connect(new MqttConnectOptions(), new Object(), new MqttActionListenerImpl(promise));

        });

    }

    private ThingFuture<Void> _disconnect() {
        return executor.promise(promise -> {

            // 断开后续动作
            promise.self()
                    .onSuccess(future -> logger.debug("{} disconnect success!", this))
                    .onFailure(future -> logger.debug("{} disconnect failure!", this, future.getException()));

            // 执行关闭
            pahoClient.disconnect(new Object(), new MqttActionListenerImpl(promise));

        });
    }

    private boolean _isConnected() {
        return pahoClient.isConnected();
    }

    @Override
    public ThingFuture<ThingMqttConnection> connect() {
        return ThingFutureUtils.uncancellable(executor.promise(connectP -> {

            // 检查客户端是否已被销毁
            synchronized (this) {
                if (isDestroyed) {
                    throw new IllegalStateException("already destroyed!");
                }
            }

            _connect()
                    .onFailure(connectP::acceptFail)
                    .onSuccess(connF -> {

                        // 断开承诺
                        final ThingPromise<Void> disP = executor.promise(promise ->
                                promise.onSuccess(future -> {

                                    // 释放断开成功诺
                                    synchronized (this) {
                                        disconnectP = null;
                                    }

                                }));

                        // 锁定断开承诺
                        synchronized (this) {

                            // 如果客户端已被销毁，则需要强制断开连接
                            // double check
                            if (isDestroyed) {
                                _disconnect().awaitUninterruptible();
                                connectP.tryException(new IllegalStateException("already destroyed!"));
                                return;
                            }

                            // 客户端正常，进行锁定操作
                            disconnectP = disP;

                        }

                        // 连接成功，返回当前连接
                        connectP.trySuccess(new ThingMqttConnection() {

                            @Override
                            public ThingFuture<Void> disconnect() {

                                // 判断当前连接是否已关闭
                                if (disP.isDone()) {
                                    return executor.promise(promise ->
                                            promise.tryException(new IllegalAccessException("connection is disconnected!")));
                                }

                                // 判断当前连接是否还有效
                                if (disconnectP != disP) {
                                    return executor.promise(promise ->
                                            promise.tryException(new IllegalAccessException("connection is invalid!")));
                                }

                                // 一切状态正常，进入关闭程序
                                return ThingFutureUtils.uncancellable(_disconnect().onSuccess(disF -> disP.trySuccess()));
                            }

                            @Override
                            public ThingFuture<Void> getDisconnectFuture() {
                                return ThingFutureUtils.uncancellable(disP);
                            }

                        });

                    });
        }));
    }

    @Override
    public void destroy() {
        synchronized (this) {

            // 如已被销毁，立即返回
            if (isDestroyed) {
                return;
            }

            // 标记已被销毁
            isDestroyed = true;

            // 如有活跃的连接，则需关闭当前连接
            if (_isConnected()) {
                _disconnect().awaitUninterruptible().onDone(future -> {

                    // 如关闭承诺被锁定，则需要通知关闭承诺完成
                    if (null != disconnectP) {
                        disconnectP.trySuccess();
                    }

                });
            }

        }
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
            executor.promise(promise -> {

                promise.self()
                        .onSuccess(future -> logger.debug("{} receive success! << topic={};", ThingMqttClientImplByPaho.this, topic))
                        .onFailure(future -> logger.debug("{} receive failure! << topic={};", ThingMqttClientImplByPaho.this, topic, future.getException()));

                handler.onMessage(topic, new ThingMqttMessageImpl(message.getQos(), message.getPayload()));
                promise.trySuccess(message);

            });
        }

    }

}
