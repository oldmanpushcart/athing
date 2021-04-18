package com.github.ompc.athing.aliyun.thing.executor.impl.modular;

import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.ThingTokenPromise;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.op.ThingMessenger;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingTokenFuture;
import com.github.ompc.athing.standard.thing.boot.Modular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static java.lang.String.format;

/**
 * 设备上报模块信息MQTT执行器
 */
public class ThingModularReportPostMqttExecutor implements MqttExecutor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingMessenger messenger;

    public ThingModularReportPostMqttExecutor(ThingImpl thing, ThingExecutor executor, ThingMessenger messenger) {
        this.thing = thing;
        this.executor = executor;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {

    }

    /**
     * 报告设备模块信息
     *
     * @param token  令牌
     * @param module 模块
     * @return future
     */
    public ThingTokenFuture<Void> reportModule(Modular module) {
        final String token = generateToken();
        return new ThingTokenPromise<>(thing, token, executor, promise -> {

            final String topic = format("/ota/device/inform/%s/%s", thing.getProductId(), thing.getThingId());
            final Object message = new MapObject()
                    .putProperty("id", token)
                    .enterProperty("params")
                    /**/.putProperty("module", module.getModuleId())
                    /**/.putProperty("version", module.getModuleVersion())
                    .exitProperty();

            promise.acceptDone(messenger.post(topic, message))
                    .onSuccess(future ->
                            logger.info("{}/module report version, req={};module={};version={};",
                                    thing,
                                    token,
                                    module.getModuleId(),
                                    module.getModuleVersion()
                            )
                    );

        });

    }

}
