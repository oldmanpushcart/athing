package com.github.ompc.athing.aliyun.thing.mqtt;

import java.nio.charset.StandardCharsets;

/**
 * 设备MQTT消息
 */
public class ThingMqttMessage {

    private final int qos;
    private final byte[] data;

    /**
     * 设备MQTT消息
     *
     * @param data 数据
     */
    public ThingMqttMessage(byte[] data) {
        this(ThingMqttClient.MQTT_QOS_AT_MOST_ONCE, data);
    }

    /**
     * 设备MQTT消息
     *
     * @param qos  QoS
     * @param data 数据
     */
    public ThingMqttMessage(int qos, byte[] data) {
        this.qos = qos;
        this.data = data;
    }

    /**
     * 获取QoS
     *
     * @return QoS
     */
    public int getQos() {
        return qos;
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 获取String数据
     *
     * @return String数据
     */
    public String getStringData() {
        return new String(getData(), StandardCharsets.UTF_8);
    }

}
