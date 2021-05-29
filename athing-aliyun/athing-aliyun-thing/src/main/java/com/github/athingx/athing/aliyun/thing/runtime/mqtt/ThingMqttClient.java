package com.github.athingx.athing.aliyun.thing.runtime.mqtt;

import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.ThingFuture;

/**
 * 设备MQTT客户端
 */
public interface ThingMqttClient extends ThingMqtt {

    /**
     * 订阅MQTT消息
     *
     * @param express 订阅表达式
     * @param handler 消息处理器
     * @return 订阅凭证
     */
    ThingFuture<Void> subscribe(String express, ThingMqttMessageHandler handler);

    /**
     * 同步订阅MQTT消息
     *
     * @param express 订阅表达式
     * @param handler 消息处理器
     * @throws ThingException 订阅失败
     */
    void syncSubscribe(String express, ThingMqttMessageHandler handler) throws ThingException;

    /**
     * 发布MQTT消息
     *
     * @param topic   主题
     * @param message 消息
     * @return 发布凭证
     */
    ThingFuture<Void> publish(String topic, ThingMqttMessage message);

    /**
     * MQTT客户端连接
     *
     * @return 连接凭证
     */
    ThingFuture<ThingMqttConnection> connect();

    /**
     * 销毁客户端
     */
    void destroy();

}
