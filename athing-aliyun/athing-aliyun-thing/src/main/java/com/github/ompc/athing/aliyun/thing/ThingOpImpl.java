package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.executor.ThingTimer;
import com.github.ompc.athing.aliyun.thing.executor.impl.ThingPostMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.ThingPropertySetMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.ThingServiceInvokeMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.config.ThingConfigPullMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.config.ThingConfigPushMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.modular.ThingModularReportPostMqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.impl.modular.ThingModularUpgradePushMqttExecutor;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingOp;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import com.github.ompc.athing.standard.thing.ThingTokenFuture;
import com.github.ompc.athing.standard.thing.boot.Modular;
import com.github.ompc.athing.standard.thing.config.ThingConfigApply;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

import java.util.concurrent.ExecutorService;

/**
 * 设备操作实现
 */
class ThingOpImpl implements ThingOp {

    private final ThingImpl thing;
    private final ExecutorService executor;
    private final ThingTimer timer;
    private final MqttExecutor[] mqttExecutors;
    private final ThingModularReportPostMqttExecutor thingModularReportPostMqttExecutor;
    private final ThingConfigPullMqttExecutor thingConfigPullMqttExecutor;
    private final ThingPostMqttExecutor thingPostMqttExecutor;

    ThingOpImpl(ThingImpl thing, ThingConnectOption connOpt, ExecutorService executor, ThingTimer timer, IMqttAsyncClient client) {
        this.thing = thing;
        this.executor = executor;
        this.timer = timer;
        final ThingMessenger messenger = new ThingMessenger(thing, timer, connOpt, client);
        mqttExecutors = new MqttExecutor[]{
                this.thingModularReportPostMqttExecutor = new ThingModularReportPostMqttExecutor(thing, messenger),
                this.thingConfigPullMqttExecutor = new ThingConfigPullMqttExecutor(thing, messenger),
                this.thingPostMqttExecutor = new ThingPostMqttExecutor(thing, messenger),
                new ThingModularUpgradePushMqttExecutor(thing, messenger),
                new ThingConfigPushMqttExecutor(thing, messenger),
                new ThingPropertySetMqttExecutor(thing, messenger),
                new ThingServiceInvokeMqttExecutor(thing, messenger),
        };
    }

    public MqttExecutor[] getMqttExecutors() {
        return mqttExecutors;
    }

    @Override
    public ThingReplyFuture<Void> postThingEvent(ThingEvent<?> event) {
        return thingPostMqttExecutor.postThingEvent(event);
    }

    @Override
    public ThingReplyFuture<Void> postThingProperties(Identifier[] identifiers) {
        return thingPostMqttExecutor.postThingProperties(identifiers);
    }

    @Override
    public ThingTokenFuture<Void> reportModule(Modular module) {
        return thingModularReportPostMqttExecutor.reportModule(module);
    }

    @Override
    public ThingReplyFuture<ThingConfigApply> updateThingConfig() {
        return thingConfigPullMqttExecutor.updateThingConfig();
    }

    @Override
    public ThingFuture<Void> reboot() {
        return new ThingPromise<>(thing, promise ->
                executor.execute(() -> {
                    try {
                        thing.getThingOpHook().reboot(thing);
                        promise.trySuccess(null);
                    } catch (Throwable cause) {
                        promise.tryException(cause);
                    }
                }));

    }

}
