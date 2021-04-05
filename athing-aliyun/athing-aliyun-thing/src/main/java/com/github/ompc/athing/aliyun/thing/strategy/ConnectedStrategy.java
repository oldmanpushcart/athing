package com.github.ompc.athing.aliyun.thing.strategy;

import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.standard.thing.Thing;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

/**
 * 设备连接成功策略
 */
public interface ConnectedStrategy extends ThingStrategy {

    /**
     * 连接成功
     *
     * @param thing   设备
     * @param connOpt 连接参数
     * @param client  MQTT客户端
     */
    void connected(Thing thing, ThingConnectOption connOpt, IMqttAsyncClient client);

}
