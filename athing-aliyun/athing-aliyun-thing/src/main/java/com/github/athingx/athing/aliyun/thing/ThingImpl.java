package com.github.athingx.athing.aliyun.thing;

import com.github.athingx.athing.aliyun.framework.Constants;
import com.github.athingx.athing.aliyun.thing.container.ThingComContainer;
import com.github.athingx.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.athingx.athing.aliyun.thing.op.ThingOpImpl;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntime;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntimes;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutorImpl;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessengerImpl;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttClient;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttConnection;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.paho.ThingMqttClientImplByPaho;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.ThingFuture;
import com.github.athingx.athing.standard.thing.ThingOp;
import com.github.athingx.athing.standard.thing.boot.ThingComLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

/**
 * 阿里云设备实现
 */
public class ThingImpl implements Thing {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThingAccess access;

    private final ThingComContainer container;
    private final ThingExecutor executor;
    private final ThingMqttClient client;
    private final ThingMessenger messenger;
    private final ThingOp op;
    private final String _string;

    private volatile boolean destroyed = false;

    ThingImpl(URI remote, ThingAccess access, ThingBootOption option) throws ThingException {
        this.access = access;
        this._string = String.format("/%s/%s", access.getProductId(), access.getThingId());
        this.executor = new ThingExecutorImpl(this, option);
        this.container = new ThingComContainer(this);
        this.client = new ThingMqttClientImplByPaho(remote, access, option, this, executor) {
            @Override
            public ThingFuture<ThingMqttConnection> connect() {
                return super.connect()

                        // 设备连网通知
                        .onSuccess(connF ->
                                container.getThingComSet(ThingComLifeCycle.class)
                                        .forEach(component -> {
                                            try {
                                                component.onConnected();
                                            } catch (Throwable cause) {
                                                logger.warn("fire onConnected occur an negligible error!", cause);
                                            }
                                        }))

                        // 设备断网通知
                        .onSuccess(connF -> connF.getSuccess().getDisconnectFuture().onSuccess(disF ->
                                container.getThingComSet(ThingComLifeCycle.class)
                                        .forEach(component -> {
                                            try {
                                                component.onDisconnected();
                                            } catch (Throwable cause) {
                                                logger.warn("fire onDisconnected occur an negligible error!", cause);
                                            }
                                        })));
            }
        };
        this.messenger = new ThingMessengerImpl(option, this, executor, client);
        this.op = new ThingOpImpl(this, container, executor, client, messenger);
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 初始化设备
     *
     * @param loaders 组件加载器
     * @throws ThingException 初始化失败
     */
    protected Thing init(Set<ThingComLoader> loaders) throws ThingException {

        // 设备运行时中添加自己，给强依赖阿里云实现的组件使用
        ThingRuntimes.append(this, new ThingRuntime() {
            @Override
            public ThingMessenger getThingMessenger() {
                return messenger;
            }

            @Override
            public ThingMqtt getThingMqtt() {
                return client;
            }

            @Override
            public ThingAccess getThingAccess() {
                return access;
            }

            @Override
            public ThingExecutor getThingExecutor() {
                return executor;
            }

        });

        // 初始化组件容器
        container.initializing(loaders);

        return this;
    }

    @Override
    public String getPlatformCode() {
        return Constants.THING_PLATFORM_CODE;
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
    public <T extends ThingCom> T getThingCom(Class<T> expect, boolean required) throws ThingException {
        return container.getThingCom(expect, required);
    }

    @Override
    public <T extends ThingCom> Set<T> getThingComSet(Class<T> expect) {
        return container.getThingComSet(expect);
    }

    @Override
    public ThingOp getThingOp() {
        return op;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public synchronized void destroy() {

        if (isDestroyed()) {
            return;
        }

        // 标记为已销毁
        destroyed = true;

        // 断开连接
        client.destroy();

        // 销毁组件容器
        container.destroy();

        // 销毁设备执行引擎
        if (executor instanceof ThingExecutorImpl) {
            ((ThingExecutorImpl) executor).shutdown();
        }

        // 从运行时中移除自己
        ThingRuntimes.remove(this);

        logger.info("{} is destroyed!", this);
    }

}
