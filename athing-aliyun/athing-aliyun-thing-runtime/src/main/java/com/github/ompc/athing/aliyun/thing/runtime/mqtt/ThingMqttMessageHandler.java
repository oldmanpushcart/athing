package com.github.ompc.athing.aliyun.thing.runtime.mqtt;

/**
 * MQTT消息处理器
 */
public interface ThingMqttMessageHandler {

    /**
     * 处理消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void onMessage(String topic, ThingMqttMessage message);

}
