package com.github.ompc.athing.aliyun.thing.strategy.impl;

import com.github.ompc.athing.aliyun.thing.ThingConnectOption;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.strategy.ConnectingStrategy;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingFutureListener;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MQTT自动连接策略
 */
public class AutoConnectingStrategy implements ConnectingStrategy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int limit;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiting = lock.newCondition();

    public AutoConnectingStrategy() {
        this.limit = -1;
    }

    public AutoConnectingStrategy(int limit) {
        this.limit = limit;
    }

    // 尝试重连
    private boolean tryConnecting(Thing thing, ThingConnectOption connOpt, IMqttAsyncClient client, int times) throws InterruptedException, ThingException {

        lock.lock();
        try {
            // 等待重新连接
            if (waiting.await(connOpt.getReconnectTimeIntervalMs(), TimeUnit.MILLISECONDS)) {
                // 等待过程中提前返回，说明外部关闭了设备，需要主动放弃重连
                logger.info("{} connecting signaled at {} times, will give up connecting!", thing, times);
                return false;
            }
        } finally {
            lock.unlock();
        }

        // 开始重新连接
        final ThingFuture<Void> connF = new ThingPromise<>(thing, promise -> {
            client.connect(new Object(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    promise.trySuccess(null);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    promise.tryException(exception);
                }
            });

            promise.onException(future ->
                    logger.warn("{} connecting occur error, will try the {} times after {} ms",
                            thing, times, connOpt.getReconnectTimeIntervalMs(), future.getException()
                    ));

        });

        // 开始重新连接
        connF.waitingForDone();

        // 重连异常
        if (connF.isException()) {

            // 不限次数重连接
            if (limit < 0 || times <= limit) {
                return false;
            }

            // 当前重连次数大于限制
            throw new ThingException(
                    thing,
                    String.format("connecting times: %s is over limit: %s", times, limit),
                    connF.getException()
            );

        }

        // 重连取消
        if (connF.isCancelled()) {
            throw new ThingException(
                    thing,
                    String.format("connecting is cancelled at {} times: %s", times)
            );
        }

        // 重连成功
        return true;

    }

    @Override
    public void connecting(Thing thing, ThingConnectOption connOpt, IMqttAsyncClient client) throws ThingException {

        // 执行策略之前，检查策略是否仍然还需要执行
        if (client.isConnected()) {
            return;
        }

        final ThingFutureListener.OnSuccess<Void> stopFutureListener = future -> stop();
        thing.getDestroyFuture().onSuccess(stopFutureListener);

        int times = 1;

        try {

            do {
                logger.warn("{}/mqtt try connecting the {} times after {} ms",
                        thing, times, connOpt.getReconnectTimeIntervalMs());
            } while (!tryConnecting(thing, connOpt, client, times++));

            // 连接成功
            logger.info("{}/mqtt connecting success at {} times!", thing, times);

        } catch (InterruptedException iCause) {

            // 重连过程中线程被中断，则说明程序可能正在重启，应立即退出重连
            logger.info("{}/mqtt connecting interrupted at {} times, will give up reconnect!", thing, times);
            Thread.currentThread().interrupt();

        } finally {
            thing.getDestroyFuture().removeListener(stopFutureListener);
        }

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
