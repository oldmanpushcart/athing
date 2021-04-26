package com.github.ompc.athing.aliyun.thing.runtime.mqtt;

public class ThingMqttMessageImpl implements ThingMqttMessage {

    private final int qos;
    private final byte[] data;

    /**
     * 设备MQTT消息
     *
     * @param data 数据
     */
    public ThingMqttMessageImpl(byte[] data) {
        this(MQTT_QOS_AT_MOST_ONCE, data);
    }

    /**
     * 设备MQTT消息
     *
     * @param qos  QoS
     * @param data 数据
     */
    public ThingMqttMessageImpl(int qos, byte[] data) {
        this.qos = qos;
        this.data = data;
    }

    @Override
    public int getQos() {
        return qos;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
