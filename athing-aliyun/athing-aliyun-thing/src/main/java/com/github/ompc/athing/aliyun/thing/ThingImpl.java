package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.op.ThingOpImpl;
import com.github.ompc.athing.aliyun.thing.runtime.ThingRuntime;
import com.github.ompc.athing.aliyun.thing.runtime.ThingRuntimes;
import com.github.ompc.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingExecutorImpl;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.ThingMessengerImpl;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.paho.ThingMqttClientImplByPaho;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.Executor;

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

    ThingImpl(final URI remote,
              final ThingAccess access,
              final ThingBootOption option,
              final Executor executor) throws ThingException {
        this.access = access;
        this._string = String.format("/%s/%s", access.getProductId(), access.getThingId());
        this.executor = new ThingExecutorImpl(this, executor);
        this.container = new ThingComContainer(this);
        this.client = new ThingMqttClientImplByPaho(remote, access, option, this, this.executor);
        this.messenger = new ThingMessengerImpl(option, this, this.executor, this.client);
        this.op = new ThingOpImpl(this, this.container, this.executor, this.client, this.messenger);
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
        container.initializing(loaders);
        return this;
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

        logger.info("{} is destroyed!", this);
    }

}
