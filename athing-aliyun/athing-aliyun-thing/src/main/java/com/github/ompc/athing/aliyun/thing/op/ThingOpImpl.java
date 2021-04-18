package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.*;
import com.github.ompc.athing.standard.thing.boot.Modular;
import com.github.ompc.athing.standard.thing.config.ThingConfigApply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备操作实现
 */
public class ThingOpImpl implements ThingOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingMqttClient client;
    private final String _string;

    private final ThingTimer timer;
    private final ThingMessenger messenger;
    private final ThingEventOp eventOp;
    private final ThingPropertyOp propertyOp;


    public ThingOpImpl(ThingBootOption option, Thing thing, ThingComContainer container, ThingExecutor executor, ThingMqttClient client) throws ThingException {
        this.client = client;
        this._string = String.format("%s/op", thing);
        this.timer = new ThingTimer(thing, executor);
        this.messenger = new ThingMessenger(option, thing, executor, client, timer);
        this.eventOp = new ThingEventOp(thing, executor, client, messenger);
        this.propertyOp = new ThingPropertyOp(thing, container, executor, client, messenger);
        new ThingServiceOp(thing, container, client, messenger);

    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public ThingReplyFuture<Void> postEvent(ThingEvent<?> event) {
        return eventOp.post(event);
    }

    @Override
    public ThingReplyFuture<Void> postProperties(Identifier[] identifiers) {
        return propertyOp.post(identifiers);
    }

    @Override
    public ThingFuture<Void> connect() {
        return client.connect();
    }

    @Override
    public ThingFuture<Void> disconnect() {
        return client.disconnect();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * 销毁
     */
    public void destroy() {
        timer.destroy();
        messenger.destroy();
        logger.info("{} is destroyed!", this);
    }

}
