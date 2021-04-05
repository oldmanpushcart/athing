package com.github.ompc.athing.aliyun.thing.strategy;

import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

/**
 * 连接策略
 */
public interface ConnectingStrategy extends ThingStrategy {

    /**
     * 连接
     *
     * @param thing   设备
     * @param connOpt 连接参数
     * @param client  MQTT客户端
     * @throws ThingException 连接异常
     */
    void connecting(Thing thing, ThingConnectOption connOpt, IMqttAsyncClient client) throws ThingException;

}
