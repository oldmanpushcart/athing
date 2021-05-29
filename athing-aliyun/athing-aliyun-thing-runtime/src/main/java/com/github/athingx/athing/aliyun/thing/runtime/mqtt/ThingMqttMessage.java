package com.github.athingx.athing.aliyun.thing.runtime.mqtt;

import java.nio.charset.Charset;

/**
 * MQTT消息
 */
public interface ThingMqttMessage {

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

    /**
     * 获取消息数据（字符串）
     *
     * @param charset 字符编码
     * @return 消息数据（字符串）
     */
    default String getStringData(Charset charset) {
        return new String(getData(), charset);
    }

}
