package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.ThingException;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

/**
 * MqttClient工厂
 */
public interface MqttClientFactory {

    /**
     * 构建MqttClient
     *
     * @param remote  远程地址
     * @param access  设备密钥
     * @param connOpt 连接参数
     * @return MqttClient
     * @throws ThingException 构建失败
     */
    IMqttAsyncClient make(String remote, ThingAccess access, ThingConnectOption connOpt) throws ThingException;

}
