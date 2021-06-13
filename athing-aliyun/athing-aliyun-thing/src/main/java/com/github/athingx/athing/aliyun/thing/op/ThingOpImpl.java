package com.github.athingx.athing.aliyun.thing.op;

import com.github.athingx.athing.aliyun.thing.container.ThingComContainer;
import com.github.athingx.athing.aliyun.thing.runtime.caller.ThingCaller;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttClient;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.thing.*;

/**
 * 设备操作实现
 */
public class ThingOpImpl implements ThingOp {

    private final ThingExecutor executor;
    private final ThingMqttClient client;
    private final String _string;

    private final ThingEventOp eventOp;
    private final ThingPropertyOp propertyOp;


    public ThingOpImpl(Thing thing, ThingComContainer container, ThingExecutor executor, ThingCaller caller, ThingMqttClient client, ThingMessenger messenger) throws ThingException {
        this.executor = executor;
        this.client = client;
        this._string = String.format("%s/op", thing);
        this.eventOp = new ThingEventOp(thing, client, messenger);
        this.propertyOp = new ThingPropertyOp(thing, container, client, messenger);
        new ThingServiceOp(thing, container, caller, client, messenger);
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
        return executor.promise(promise ->
                client.connect()
                        .onFailure(promise::acceptFail)
                        .onSuccess(connF -> promise.trySuccess(connF.getSuccess())));
    }

}
