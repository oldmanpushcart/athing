package com.github.ompc.athing.aliyun.thing.component.mqtt.impl;

import com.github.ompc.athing.aliyun.thing.component.mqtt.Mqtt;
import com.github.ompc.athing.aliyun.thing.component.mqtt.MqttMessage;
import com.github.ompc.athing.aliyun.thing.component.mqtt.MqttMessageHandler;
import com.github.ompc.athing.aliyun.thing.component.mqtt.MqttThingCom;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttMessage;

import java.util.concurrent.Future;

/**
 * MQTT组件实现
 */
public class MqttThingComImpl implements MqttThingCom {

    private final ThingMqttClient client;

    public MqttThingComImpl(ThingMqttClient client) {
        this.client = client;
    }

    @Override
    public Mqtt getMqtt() {
        return new Mqtt() {
            @Override
            public Future<Void> publish(String topic, int qos, MqttMessage message) {
                return client.publish(topic, new ThingMqttMessage(message.getQos(), message.getData()));
            }

            @Override
            public Future<Void> subscribe(String express, MqttMessageHandler handler) {
                return client.subscribe(express, (topic, message) -> handler.onMessage(topic, new MqttMessage() {
                    @Override
                    public int getQos() {
                        return message.getQos();
                    }

                    @Override
                    public byte[] getData() {
                        return message.getData();
                    }
                }));
            }
        };
    }

}
