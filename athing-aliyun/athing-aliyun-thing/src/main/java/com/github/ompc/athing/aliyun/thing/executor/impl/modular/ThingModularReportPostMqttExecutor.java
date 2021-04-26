package com.github.ompc.athing.aliyun.thing.executor.impl.modular;

/**
 * 设备上报模块信息MQTT执行器
 */
public class ThingModularReportPostMqttExecutor {

//    private final Logger logger = LoggerFactory.getLogger(getClass());
//    private final Thing thing;
//    private final ThingExecutor executor;
//    private final ThingMessenger messenger;
//
//    public ThingModularReportPostMqttExecutor(ThingImpl thing, ThingExecutor executor, ThingMessenger messenger) {
//        this.thing = thing;
//        this.executor = executor;
//        this.messenger = messenger;
//    }
//
//    @Override
//    public void init(MqttSubscriber subscriber) throws ThingException {
//
//    }
//
//    /**
//     * 报告设备模块信息
//     *
//     * @param module 模块
//     * @return future
//     */
//    public ThingTokenFuture<Void> reportModule(Modular module) {
//        final String token = generateToken();
//        return ThingPromise.fulfill(new ThingTokenPromise<>(thing, token, executor), promise -> {
//            final String topic = format("/ota/device/inform/%s/%s", thing.getProductId(), thing.getThingId());
//            final Object message = new MapObject()
//                    .putProperty("id", token)
//                    .enterProperty("params")
//                    /**/.putProperty("module", module.getModuleId())
//                    /**/.putProperty("version", module.getModuleVersion())
//                    .exitProperty();
//
//            promise.acceptDone(messenger.post(topic, message))
//                    .onSuccess(future ->
//                            logger.info("{}/module report version, req={};module={};version={};",
//                                    thing,
//                                    token,
//                                    module.getModuleId(),
//                                    module.getModuleVersion()
//                            )
//                    );
//        });
//
//    }

}
