package com.github.ompc.athing.aliyun.thing.component.mqtt;

/**
 * MQTT消息
 */
public interface MqttMessage {

    /**
     * 获取QoS
     *
     * @return QoS
     */
    int getQos();

    /**
     * 获取消息数据
     *
     * @return 消息数据
     */
    byte[] getData();

}
