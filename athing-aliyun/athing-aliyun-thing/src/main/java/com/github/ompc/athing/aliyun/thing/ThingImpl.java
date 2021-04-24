package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.component.access.impl.AccessThingComImpl;
import com.github.ompc.athing.aliyun.thing.component.alink.impl.AlinkThingComImpl;
import com.github.ompc.athing.aliyun.thing.component.mqtt.impl.MqttThingComImpl;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.mqtt.paho.ThingMqttClientImplByPaho;
import com.github.ompc.athing.aliyun.thing.op.ThingOpImpl;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingOp;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
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

    private final URI remote;
    private final ThingAccess access;
    private final ThingBootOption option;

    private final ThingComContainer container;
    private final ThingExecutor executor;
    private final ThingMqttClient client;
    private final ThingOp op;
    private final String _string;

    private volatile boolean destroyed = false;

    ThingImpl(final URI remote,
              final ThingAccess access,
              final ThingBootOption option,
              final Executor executor) throws ThingException {
        this.remote = remote;
        this.access = access;
        this.option = option;
        this._string = String.format("/%s/%s", access.getProductId(), access.getThingId());

        this.executor = new ThingExecutor(this, executor);
        this.container = new ThingComContainer(this);
        this.client = new ThingMqttClientImplByPaho(remote, access, option, this, this.executor);
        this.op = new ThingOpImpl(option, this, container, this.executor, this.client);
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

        // 追加默认组件
        loaders.add((productId, thingId) -> new ThingCom[]{
                new AccessThingComImpl(access),
                new AlinkThingComImpl(),
                new MqttThingComImpl(client)
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

        // 销毁设备操作
        if (op instanceof ThingOpImpl) {
            ((ThingOpImpl) op).destroy();
        }

        // 销毁组件容器
        container.destroy();

        logger.info("{} is destroyed!", this);
    }

}
