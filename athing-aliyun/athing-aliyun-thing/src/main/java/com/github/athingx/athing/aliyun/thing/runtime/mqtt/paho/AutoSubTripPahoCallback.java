package com.github.athingx.athing.aliyun.thing.runtime.mqtt.paho;

import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttClient;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 自动订阅三元组
 */
public class AutoSubTripPahoCallback implements MqttCallbackExtended {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingMqttClient client;
    private final IMqttAsyncClient pahoClient;
    private final Set<PahoSubTrip> trips;

    public AutoSubTripPahoCallback(ThingMqttClient client, IMqttAsyncClient pahoClient, Set<PahoSubTrip> trips) {
        this.client = client;
        this.pahoClient = pahoClient;
        this.trips = trips;
    }

    /**
     * 订阅消息主题
     *
     * @param listener 动作监听器
     * @throws MqttException 订阅失败
     */
    private void _subscribe(IMqttActionListener listener) throws MqttException {
        pahoClient.subscribe(
                trips.stream().map(PahoSubTrip::getExpress).toArray(String[]::new),
                trips.stream().mapToInt(PahoSubTrip::getQos).toArray(),
                new Object(),
                listener,
                trips.stream().map(PahoSubTrip::getListener).toArray(IMqttMessageListener[]::new)
        );
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

        // 如果三元组中尚未有订阅，则放弃
        if (trips.isEmpty()) {
            return;
        }

        // 恢复订阅
        try {

            _subscribe(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    logger.info("{} recovery subscribe topics success", client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logger.warn("{} recovery subscribe topics failure, client will be reconnect!", client, exception);
                    throw new RuntimeException("recovery subscribe topics failure!", exception);
                }

            });
        } catch (MqttException cause) {
            logger.warn("{} recovery subscribe topics occur error, client will be reconnect!", client, cause);
            throw new RuntimeException("recovery subscribe topics error!", cause);
        }

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
