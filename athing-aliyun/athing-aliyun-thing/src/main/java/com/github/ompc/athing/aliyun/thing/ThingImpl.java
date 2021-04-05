package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.container.ThingComContainerImpl;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.executor.ThingTimer;
import com.github.ompc.athing.aliyun.thing.strategy.ConnectedStrategy;
import com.github.ompc.athing.aliyun.thing.strategy.ConnectingStrategy;
import com.github.ompc.athing.aliyun.thing.strategy.ThingStrategyManager;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingOp;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
    private final ThingStrategyManager thingStrategyManager;
    private final ThingImpl _this;
    private final ThingPromise<Thing> destroyP;

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
              final Set<ThingComLoader> thingComLoaders,
              final ThingStrategyManager thingStrategyManager) {
        super(access.getProductId(), access.getThingId());
        this.remote = remote;
        this.access = access;
        this.mcFactory = mcFactory;
        this.configListener = configListener;
        this.opHook = opHook;
        this.connOpt = connOpt;
        this.thingComLoaders = thingComLoaders;
        this.thingStrategyManager = thingStrategyManager;
        this._this = this;
        this.destroyP = new ThingPromise<>(this);
        this.destroyP.onSuccess(future -> logger.info("{} destroy completed!", this));
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
    private void init() throws ThingException {
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
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String remote) {
                    thingStrategyManager.getThingStrategies(ConnectedStrategy.class).forEach(strategy ->
                            strategy.connected(_this, connOpt, client));
                }

                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("{}/mqtt connection is lost!", _this, cause);
                    thingStrategyManager.getThingStrategies(ConnectingStrategy.class).forEach(strategy -> {
                        try {
                            strategy.connecting(_this, connOpt, client);
                        } catch (ThingException tCause) {
                            logger.warn("{}/mqtt connection is lost, reconnect strategy is invalid, will be destroy!",
                                    _this, tCause
                            );
                            destroy();
                        }
                    });
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

    /**
     * 设备连接
     *
     * @return 连接future
     */
    protected ThingFuture<Thing> connect() throws ThingException {

        return new ThingPromise<Thing>(_this, promise -> {

            // 初始化设备
            init();
            logger.debug("{} init success", _this);

            // 使用策略重连
            thingStrategyManager.getThingStrategies(ConnectingStrategy.class).forEach(strategy -> {
                try {
                    strategy.connecting(_this, connOpt, client);
                    logger.debug("{} connect success", _this);
                    promise.trySuccess(_this);
                } catch (ThingException cause) {
                    promise.tryException(new ThingException(_this, "connecting strategy is invalid", cause));
                }
            });

        }) {

            @Override
            public boolean tryException(Throwable cause) {
                return super.tryException(new ThingException(_this, "connect failure!", cause));
            }

        };

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


    /*
     * 设备关闭的严格流程
     * 1. 断开设备与平台的连接
     * 2. 关闭工作线程池
     * 3. 销毁设备组件
     */
    private void _destroy() {

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

    }

    @Override
    public ThingFuture<Thing> destroy() {

        final ThingPromise<Thing> promise = new ThingPromise<>(_this);
        final Thread destroyTh = new Thread(() -> {

            if (destroyP.isDone()) {
                return;
            }

            try {
                synchronized (_this) {
                    if (destroyP.isDone()) {
                        return;
                    }
                    _destroy();
                    destroyP.setSuccess(_this);
                    promise.trySuccess(_this);
                }
            } catch (Exception cause) {
                promise.tryException(cause);
            }

        });

        destroyTh.setDaemon(true);
        destroyTh.setName(String.format("%s-destroy-daemon", _this));
        destroyTh.start();

        return promise;

    }

    @Override
    public ThingFuture<Thing> getDestroyFuture() {
        return destroyP;
    }

    @Override
    public String toString() {
        return String.format("thing:/%s/%s", getProductId(), getThingId());
    }

}
