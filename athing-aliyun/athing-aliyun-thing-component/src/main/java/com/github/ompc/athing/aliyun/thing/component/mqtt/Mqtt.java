package com.github.ompc.athing.aliyun.thing.component.mqtt;

import java.util.concurrent.Future;

/**
 * MQTTa
 */
public interface Mqtt {

    /**
     * 最多一次
     */
    int MQTT_QOS_AT_MOST_ONCE = 0;

    /**
     * 至少一次
     */
    int MQTT_QOS_AT_LEAST_ONCE = 1;

    /**
     * 确保只有一次
     */
    int MQTT_QOS_EXACTLY_ONCE = 2;

    /**
     * 发布MQTT消息
     *
     * @param topic   主题
     * @param message 消息
     * @return 发布凭证
     */
    Future<Void> publish(String topic, MqttMessage message);

    /**
     * 订阅MQTT消息
     *
     * @param express 订阅表达式
     * @param handler 消息处理器
     * @return 订阅凭证
     */
    Future<Void> subscribe(String express, MqttMessageHandler handler);

}
