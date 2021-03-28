package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.container.ThingComContainerImpl;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingOp;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阿里云设备实现
 */
public class ThingImpl extends ThingComContainerImpl implements Thing {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingAccess access;

    private final String serverUrl;
    private final ThingConfigListener configListener;
    private final ThingOpHook opHook;
    private final ThingConnectOption connOpt;
    private final ReentrantLock clientReConnLock = new ReentrantLock();
    private final Condition clientReConnWaitingCondition = clientReConnLock.newCondition();

    private IMqttClient client;
    private ExecutorService workers;

    private ThingOpImpl thingOp;

    /**
     * 设备实现
     *
     * @param access         连接密钥
     * @param configListener 设备配置监听器
     * @param opHook         设备操作钩子
     * @param connOpt        连接选项
     */
    ThingImpl(final String serverUrl,
              final ThingAccess access,
              final ThingConfigListener configListener,
              final ThingOpHook opHook,
              final ThingConnectOption connOpt,
              final Set<ThingComLoader> loaders) throws ThingException {
        super(access.getProductId(), access.getThingId(), loaders);
        this.serverUrl = serverUrl;
        this.access = access;
        this.configListener = configListener;
        this.opHook = opHook;
        this.connOpt = connOpt;
    }

    public ThingConfigListener getThingConfigListener() {
        return configListener;
    }

    public ThingOpHook getThingOpHook() {
        return opHook;
    }

    public ThingConnectOption getThingConnOpt() {
        return connOpt;
    }

    /**
     * 初始化设备
     * <p>
     * 这里将初始化设备方法暴露出来主要是考虑到初始化时候的报错，
     * 当初始化失败时可直接销毁设备
     * </p>
     */
    protected void init() throws ThingException {
        try {
            this.workers = Executors.newFixedThreadPool(connOpt.getThreads(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread worker = new Thread(r, String.format("%s/worker-daemon", this));
                    worker.setDaemon(true);
                    return worker;
                }
            });
            this.client = new MqttClientExt(serverUrl, access);
            this.thingOp = new ThingOpImpl(this, client);

            // 配置MQTT客户端
            client.setCallback(new MqttCallbackExtended() {

                private final AtomicInteger reConnCntRef = new AtomicInteger(1);

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    for (final MqttExecutor mqttExecutor : thingOp.getMqttExecutors()) {
                        try {
                            mqttExecutor.init((topicExpress, handler) -> {
                                try {
                                    client.subscribe(topicExpress, (topic, message) -> workers.submit(() -> {
                                        try {
                                            logger.debug("{}/mqtt received message: {} -> {}", ThingImpl.this, topic, message);
                                            handler.handle(topic, message);
                                        } catch (Throwable cause) {
                                            logger.warn("{}/mqtt consume message failure, topic={};message={};", ThingImpl.this, topic, message, cause);
                                        }
                                    }));
                                    logger.debug("{}/mqtt subscribe topic: {} success", this, topicExpress);
                                } catch (MqttException cause) {
                                    throw new ThingException(
                                            ThingImpl.this,
                                            String.format("subscribe topic: %s occur error!", topicExpress),
                                            cause
                                    );
                                }
                            });
                        } catch (ThingException cause) {
                            throw new RuntimeException("init mqtt-executor occur error!", cause);
                        }
                    }

                    logger.info("{}/mqtt connect success at {} times", ThingImpl.this, reConnCntRef.getAndSet(1));

                }

                @Override
                public void connectionLost(Throwable cause) {

                    logger.warn("{}/mqtt connection lost, try reconnect the {} times after {} ms",
                            ThingImpl.this,
                            reConnCntRef.getAndAdd(1),
                            connOpt.getReconnectTimeIntervalMs(),
                            cause
                    );

                    while (true) {
                        clientReConnLock.lock();
                        try {

                            // 等待重新连接
                            if (!clientReConnWaitingCondition.await(connOpt.getReconnectTimeIntervalMs(), TimeUnit.MICROSECONDS)) {
                                // 等待过程中提前返回，说明外部关闭了设备，需要主动放弃重连
                                logger.info("{}/mqtt give up waiting reconnect", ThingImpl.this);
                                break;
                            }

                            // 开始重新连接
                            client.reconnect();

                            // 连接成功则跳出循环
                            break;
                        }

                        // 重连过程中再次发生异常，继续重连
                        catch (MqttException mCause) {
                            logger.warn("{} reconnect occur error, will try the {} times after {} ms",
                                    ThingImpl.this,
                                    reConnCntRef.getAndAdd(1),
                                    connOpt.getReconnectTimeIntervalMs()
                            );
                        }

                        // 重连过程中线程被中断，则说明程序可能正在重启，应立即退出重连
                        catch (InterruptedException iCause) {
                            logger.info("{}/mqtt interrupt waiting reconnect", ThingImpl.this);
                            Thread.currentThread().interrupt();
                            break;
                        } finally {
                            clientReConnLock.unlock();
                        }
                    }

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }

            });

            // 初始化组件
            initContainer(this);

        } catch (Exception cause) {
            throw new ThingException(this, "init occur error!", cause);
        }

    }

    protected void connect() throws ThingException {
        try {

            // 客户端建立连接
            client.connect(new MqttConnectOptions() {{
                setCleanSession(true);
                setConnectionTimeout((int) (connOpt.getConnectTimeoutMs() / 1000));
                setKeepAliveInterval((int) (connOpt.getKeepAliveIntervalMs() / 1000));
                setAutomaticReconnect(false);
            }});

        } catch (MqttException cause) {
            throw new ThingException(this, String.format("connect server: %s occur error!", serverUrl),
                    cause
            );
        }
    }

    @Override
    public String getProductId() {
        return access.getProductId();
    }

    @Override
    public String getThingId() {
        return access.getThingId();
    }


    @Override
    public ThingOp getThingOp() {
        return thingOp;
    }

    @Override
    public void destroy() {

        /*
         * 设备关闭的严格流程
         * 1. 断开设备与平台的连接
         * 2. 关闭工作线程池
         * 3. 销毁设备组件
         */

        // 断开连接
        if (null != client) {

            // 断开MQTT客户端连接
            try {
                client.disconnect();
            } catch (MqttException cause) {
                logger.warn("{}/mqtt disconnect occur error!", this, cause);
            }
            // 通知重连接不再等待
            clientReConnLock.lock();
            try {
                clientReConnWaitingCondition.signalAll();
            } finally {
                clientReConnLock.unlock();
            }
        }

        // 关闭线程池
        if (null != workers) {
            workers.shutdown();
        }

        // 销毁组件容器
        destroyContainer();

        logger.info("{} destroy completed!", this);

    }

    @Override
    public String toString() {
        return String.format("thing:/%s/%s", getProductId(), getThingId());
    }

}
