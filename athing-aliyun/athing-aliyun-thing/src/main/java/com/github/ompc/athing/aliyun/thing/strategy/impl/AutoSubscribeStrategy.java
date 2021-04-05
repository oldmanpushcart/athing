package com.github.ompc.athing.aliyun.thing.strategy.impl;

import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.aliyun.thing.strategy.ConnectedStrategy;
import com.github.ompc.athing.standard.thing.Thing;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class AutoSubscribeStrategy implements ConnectedStrategy {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<SubTriplet> trips = new LinkedHashSet<>();

    /**
     * 订阅MQTT消息
     *
     * @param topic    消息主题
     * @param listener 消息监听器
     * @return this
     */
    public AutoSubscribeStrategy subscribe(String topic, IMqttMessageListener listener) {
        trips.add(new SubTriplet(topic, 2, listener));
        return this;
    }

    /**
     * 订阅消息主题
     *
     * @param listener 动作监听器
     * @throws MqttException 订阅失败
     */
    private void _subscribe(IMqttAsyncClient client, IMqttActionListener listener) throws MqttException {
        client.subscribe(
                trips.stream().map(triplet -> triplet.topic).toArray(String[]::new),
                trips.stream().mapToInt(triplet -> triplet.qos).toArray(),
                new Object(),
                listener,
                trips.stream().map(triplet -> triplet.listener).toArray(IMqttMessageListener[]::new)
        );
    }

    @Override
    public void connected(Thing thing, ThingConnectOption connOpt, IMqttAsyncClient client) {

        try {
            _subscribe(client, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    trips.forEach(trip -> logger.debug("{}/mqtt subscribe topic: {}", thing, trip.topic));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logger.warn("{}/mqtt subscribe topic failure, thing will be reconnect!", thing, exception);
                    throw new RuntimeException("subscribe topic failure!", exception);
                }

            });
        } catch (MqttException cause) {
            logger.warn("{}/mqtt subscribe topic occur error, thing will be reconnect!", thing, cause);
            throw new RuntimeException("subscribe topic error!", cause);
        }

    }


    /**
     * 订阅三元组
     */
    private static class SubTriplet {

        final String topic;
        final int qos;
        final IMqttMessageListener listener;

        SubTriplet(String topic, int qos, IMqttMessageListener listener) {
            this.topic = topic;
            this.qos = qos;
            this.listener = listener;
        }

    }

}
