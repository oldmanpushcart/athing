package com.github.ompc.athing.aliyun.thing.component.mqtt;

/**
 * MQTT消息处理器
 */
public interface MqttMessageHandler {

    /**
     * 处理消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void onMessage(String topic, MqttMessage message);

}
