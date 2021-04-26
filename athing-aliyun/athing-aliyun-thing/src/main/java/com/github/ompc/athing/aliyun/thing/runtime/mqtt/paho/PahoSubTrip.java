package com.github.ompc.athing.aliyun.thing.runtime.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

/**
 * Paho订阅三元组
 */
public class PahoSubTrip {

    private final String express;
    private final int qos;
    private final IMqttMessageListener listener;

    /**
     * Paho订阅三元组
     *
     * @param express  订阅表达式
     * @param qos      QoS
     * @param listener 消息监听器
     */
    public PahoSubTrip(String express, int qos, IMqttMessageListener listener) {
        this.express = express;
        this.qos = qos;
        this.listener = listener;
    }

    /**
     * 获取订阅表达式
     *
     * @return 订阅表达式
     */
    public String getExpress() {
        return express;
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
     * 获取消息监听器
     *
     * @return 消息监听器
     */
    public IMqttMessageListener getListener() {
        return listener;
    }

}
