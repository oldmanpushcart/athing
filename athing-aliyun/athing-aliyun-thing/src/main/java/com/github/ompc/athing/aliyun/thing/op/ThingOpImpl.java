package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备操作实现
 */
public class ThingOpImpl implements ThingOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingMqttClient client;
    private final String _string;

    private final ThingTimer timer;
    private final ThingMessenger messenger;
    private final ThingEventOp eventOp;
    private final ThingPropertyOp propertyOp;


    public ThingOpImpl(ThingBootOption option, Thing thing, ThingComContainer container, ThingExecutor executor, ThingMqttClient client) throws ThingException {
        this.thing = thing;
        this.executor = executor;
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
    public ThingFuture<ThingConnection> connect() {
        return new ThingPromise<>(thing, executor, promise ->
                client.connect()
                        .onFailure(promise::acceptFail)
                        .onSuccess(connF -> promise.trySuccess(connF.getSuccess())));
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
