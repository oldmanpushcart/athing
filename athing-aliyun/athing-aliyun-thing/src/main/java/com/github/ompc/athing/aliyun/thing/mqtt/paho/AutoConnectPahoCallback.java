package com.github.ompc.athing.aliyun.thing.mqtt.paho;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFutureListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;

/**
 * 自动重连回调
 */
public class AutoConnectPahoCallback implements MqttCallbackExtended {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingBootOption option;
    private final Thing thing;
    private final ThingMqttClient client;
    private final IMqttAsyncClient pahoClient;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiting = lock.newCondition();

    public AutoConnectPahoCallback(ThingBootOption option, Thing thing, ThingMqttClient client, IMqttAsyncClient pahoClient) {
        this.option = option;
        this.thing = thing;
        this.client = client;
        this.pahoClient = pahoClient;
    }

    // 尝试重连
    private boolean tryConnecting(int times) throws InterruptedException {

        try {

            // 客户端建连并阻塞等待连接完成
            pahoClient.connect().waitForCompletion();

        } catch (Exception cause) {

            // 连接失败，重新尝试进行连接
            logger.warn("{} connecting occur error, will try the {} times after {} ms", thing, times, option.getReconnectTimeIntervalMs(), cause);
            return false;

        }

        // 重连成功
        return true;

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }

    @Override
    public void connectionLost(Throwable cause) {

        int times = 1;

        try {

            do {

                // 等待指定的时间间隔后再进行尝试
                lock.lock();
                try {
                    // 等待重新连接
                    if (waiting.await(option.getReconnectTimeIntervalMs(), TimeUnit.MILLISECONDS)) {
                        // 等待过程中提前返回，说明外部关闭了设备，需要主动放弃重连
                        logger.info("{} give up connecting at {} times, maybe destroyed!", client, times);
                        return;
                    }
                } finally {
                    lock.unlock();
                }

                logger.warn("{} try connecting the {} times after {} ms", client, times, option.getReconnectTimeIntervalMs());

            } while (!tryConnecting(times++));

            // 连接成功
            logger.info("{} connecting success at {} times!", client, times);

        } catch (InterruptedException iCause) {

            // 重连过程中线程被中断，则说明程序可能正在重启，应立即退出重连
            logger.info("{}/mqtt connecting interrupted at {} times, will give up reconnect!", thing, times);
            Thread.currentThread().interrupt();

        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /**
     * 停止重连策略
     */
    private void stop() {

        lock.lock();
        try {
            waiting.signal();
        } finally {
            lock.unlock();
        }

    }

}
