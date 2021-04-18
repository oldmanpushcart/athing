package com.github.ompc.athing.aliyun.thing.mqtt;

/**
 * 设备MQTT消息处理器
 */
public interface ThingMqttMessageHandler {

    /**
     * 处理消息
     *
     * @param message 消息
     */
    void onMessage(String topic, ThingMqttMessage message);

}
