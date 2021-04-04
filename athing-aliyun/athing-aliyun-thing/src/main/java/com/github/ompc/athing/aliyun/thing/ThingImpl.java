package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.container.ThingComContainerImpl;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.executor.ThingTimer;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingOp;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private final String remote;
    private final ThingAccess access;
    private final MqttClientFactory mcFactory;


    private final ThingConfigListener configListener;
    private final ThingOpHook opHook;
    private final ThingConnectOption connOpt;
    private final ReentrantLock clientReConnLock = new ReentrantLock();
    private final Condition clientReConnWaitingCondition = clientReConnLock.newCondition();
    private final Set<ThingComLoader> thingComLoaders;
    private final ThingImpl _this;

    private IMqttAsyncClient client;
    private ExecutorService executor;
    private ThingTimer timer;

    private ThingOpImpl thingOp;

    /**
     * 设备实现
     *
     * @param remote         远程地址
     * @param access         连接密钥
     * @param mcFactory      MQTT客户端工厂
     * @param configListener 设备配置监听器
     * @param opHook         设备操作钩子
     * @param connOpt        连接选项
     */
    ThingImpl(final String remote,
              final ThingAccess access,
              final MqttClientFactory mcFactory,
              final ThingConfigListener configListener,
              final ThingOpHook opHook,
              final ThingConnectOption connOpt,
              final Set<ThingComLoader> thingComLoaders) {
        super(access.getProductId(), access.getThingId());
        this.remote = remote;
        this.access = access;
        this.mcFactory = mcFactory;
        this.configListener = configListener;
        this.opHook = opHook;
        this.connOpt = connOpt;
        this.thingComLoaders = thingComLoaders;
        this._this = this;
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

    private MqttCallback genMqttCb() {
        return new MqttCallbackExtended() {

            private final AtomicInteger reConnCntRef = new AtomicInteger(1);

            /**
             * 订阅消息主题
             * @param triplets 订阅三元组
             * @param listener 动作监听器
             * @throws MqttException 订阅失败
             */
            private void subscribe(List<SubscribeTriplet> triplets, IMqttActionListener listener) throws MqttException {
                client.subscribe(
                        triplets.stream().map(triplet -> triplet.topic).toArray(String[]::new),
                        triplets.stream().mapToInt(triplet -> triplet.qos).toArray(),
                        new Object(),
                        listener,
                        triplets.stream().map(triplet -> triplet.listener).toArray(IMqttMessageListener[]::new)
                );
            }

            /**
             * 生成订阅三元组集合
             * @return 订阅三元组集合
             */
            private List<SubscribeTriplet> genSubTrips() {
                final List<SubscribeTriplet> triplets = new ArrayList<>();
                Arrays.stream(thingOp.getMqttExecutors()).forEach(mqttExecutor -> {
                    try {
                        mqttExecutor.init((topicExpress, handler) ->
                                triplets.add(new SubscribeTriplet(topicExpress, 0, (topic, message) -> {
                                    try {
                                        logger.debug("{}/mqtt received message: {} -> {}", _this, topic, message);
                                        handler.handle(topic, message);
                                    } catch (Throwable cause) {
                                        logger.warn("{}/mqtt consume message failure, topic={};message={};", _this, topic, message, cause);
                                    }
                                }))
                        );
                    } catch (ThingException cause) {
                        throw new RuntimeException("init mqtt-executor occur error!", cause);
                    }
                });
                return triplets;
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                // 生成订阅三元组
                final List<SubscribeTriplet> subTrips = genSubTrips();

                // 订阅promise
                final ThingPromise<Void> subP = new ThingPromise<Void>(_this, promise -> {

                    subscribe(subTrips, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            promise.trySuccess(null);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            promise.tryException(exception);
                        }
                    });

                    // 订阅成功
                    promise.onSuccess(future -> subTrips.forEach(trip -> logger.debug("{}/mqtt subscribe topic: {} success", _this, trip.topic)));

                }) {

                    @Override
                    public boolean tryException(Throwable cause) {
                        return super.tryException(new ThingException(_this, "subscribe mqtt-topic occur error!", cause));
                    }

                };

                // 阻塞等待订阅完成
                try {
                    // subP.sync();
                    if (subP.isException()) {
                        throw subP.getException();
                    }
                } catch (Throwable cause) {
                    throw new RuntimeException("subscribe mqtt-topic occur error!", cause);
                }

                logger.info("{}/mqtt connect success at {} times", _this, reConnCntRef.getAndSet(1));

            }

            @Override
            public void connectionLost(Throwable cause) {

                logger.warn("{}/mqtt connection lost, try reconnect the {} times after {} ms",
                        _this,
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
                            logger.info("{}/mqtt give up waiting reconnect", _this);
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
                                _this,
                                reConnCntRef.getAndAdd(1),
                                connOpt.getReconnectTimeIntervalMs()
                        );
                    }

                    // 重连过程中线程被中断，则说明程序可能正在重启，应立即退出重连
                    catch (InterruptedException iCause) {
                        logger.info("{}/mqtt interrupt waiting reconnect", _this);
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

            /**
             * 订阅三元组
             */
            class SubscribeTriplet {

                final String topic;
                final int qos;
                final IMqttMessageListener listener;

                SubscribeTriplet(String topic, int qos, IMqttMessageListener listener) {
                    this.topic = topic;
                    this.qos = qos;
                    this.listener = listener;
                }

            }

        };
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

            // 加载设备组件
            loading(thingComLoaders);

            // 初始化工作线程池
            this.executor = Executors.newFixedThreadPool(connOpt.getThreads(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread worker = new Thread(r, String.format("%s/worker-daemon", this));
                    worker.setDaemon(true);
                    return worker;
                }
            });

            // 初始化客户端
            this.client = mcFactory.make(remote, access, connOpt);

            // 初始化定时器
            this.timer = new ThingTimer(this, executor);

            // 初始化设备操作
            this.thingOp = new ThingOpImpl(this, connOpt, executor, timer, client);

            // 配置MQTT客户端
            client.setCallback(genMqttCb());

            // 初始化组件
            initContainer(this);

        } catch (Exception cause) {
            throw new ThingException(this, "init occur error!", cause);
        }

    }

    /**
     * 设备连接
     *
     * @return 连接future
     */
    protected ThingFuture<Void> connect() throws ThingException {

        return new ThingPromise<>(_this, promise -> {

            // 客户端建立连接
            client.connect(new MqttConnectOptions(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    promise.trySuccess(null);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    promise.tryException(exception);
                }
            });

        });

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

        // 关闭定时器
        if (null != timer) {
            timer.destroy();
        }

        // 关闭线程池
        if (null != executor) {
            executor.shutdown();
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
