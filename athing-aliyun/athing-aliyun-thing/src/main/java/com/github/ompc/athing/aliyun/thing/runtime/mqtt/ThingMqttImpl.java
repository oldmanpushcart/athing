package com.github.ompc.athing.aliyun.thing.runtime.mqtt;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.paho.ThingMqttClientImplByPaho;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;

import java.net.URI;

/**
 * 设备MQTT实现
 */
public class ThingMqttImpl implements ThingMqtt {

    private final ThingMqttClient client;

    /**
     * 设备MQTT实现
     *
     * @param remote   远程地址
     * @param access   设备接入
     * @param option   启动选项
     * @param thing    设备
     * @param executor 设备执行器
     * @throws ThingException 构建异常
     */
    public ThingMqttImpl(URI remote, ThingAccess access, ThingBootOption option, Thing thing, ThingExecutor executor) throws ThingException {
        this.client = new ThingMqttClientImplByPaho(remote, access, option, thing, executor);
    }

    @Override
    public ThingFuture<Void> publish(String topic, ThingMqttMessage message) {
        return client.publish(topic, message);
    }

    @Override
    public ThingFuture<Void> subscribe(String express, ThingMqttMessageHandler handler) {
        return client.subscribe(express, handler);
    }

    @Override
    public void syncSubscribe(String express, ThingMqttMessageHandler handler) throws ThingException {
        client.syncSubscribe(express, handler);
    }

}
