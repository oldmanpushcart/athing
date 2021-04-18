package com.github.ompc.athing.aliyun.thing.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

/**
 * Paho回调组
 */
public class GroupPahoCallback implements MqttCallbackExtended {

    private final MqttCallbackExtended[] callbacks;

    public GroupPahoCallback(List<MqttCallbackExtended> callbacks) {
        this.callbacks = callbacks.toArray(new MqttCallbackExtended[0]);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        for (MqttCallbackExtended callback : callbacks) {
            callback.connectComplete(reconnect, serverURI);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        for (MqttCallbackExtended callback : callbacks) {
            callback.connectionLost(cause);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        for (MqttCallbackExtended callback : callbacks) {
            callback.messageArrived(topic, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        for (MqttCallbackExtended callback : callbacks) {
            callback.deliveryComplete(token);
        }
    }

}
