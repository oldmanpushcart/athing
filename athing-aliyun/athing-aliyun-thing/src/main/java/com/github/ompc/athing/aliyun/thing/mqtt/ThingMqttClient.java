package com.github.ompc.athing.aliyun.thing.mqtt;

import com.github.ompc.athing.aliyun.thing.ThingAccess;
import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.mqtt.paho.*;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingConnection;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * 设备MQTT客户端
 */
public interface ThingMqttClient {

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
