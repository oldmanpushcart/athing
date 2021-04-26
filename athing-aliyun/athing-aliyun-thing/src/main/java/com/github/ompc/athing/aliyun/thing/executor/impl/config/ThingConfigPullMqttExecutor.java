package com.github.ompc.athing.aliyun.thing.executor.impl.config;

/**
 * 设备配置主动请求执行器
 */
public class ThingConfigPullMqttExecutor {

//    private final Logger logger = LoggerFactory.getLogger(getClass());
//    private final ThingImpl thing;
//    private final ThingExecutor executor;
//    private final ThingMessenger messenger;
//
//    private final Gson gson = GsonFactory.getGson();
//
//    public ThingConfigPullMqttExecutor(ThingImpl thing, ThingExecutor executor, ThingMessenger messenger) {
//        this.thing = thing;
//        this.executor = executor;
//        this.messenger = messenger;
//    }
//
//    @Override
//    public void init(MqttSubscriber subscriber) throws ThingException {
//        subscriber.subscribe(
//                format("/sys/%s/%s/thing/config/get_reply", thing.getProductId(), thing.getThingId()),
//                this
//        );
//    }
//
//    @Override
//    public void handle(String mqttTopic, MqttMessage mqttMessage) {
//
//        final AlinkReply<ThingConfigPullData> reply = gson.fromJson(
//                new String(mqttMessage.getPayload(), UTF_8),
//                new TypeToken<AlinkReply<ThingConfigPullData>>() {
//                }.getType()
//        );
//
//        final String token = reply.getReqId();
//        logger.debug("{}/config/pull receive reply, req={};code={};message={};",
//                thing, token, reply.getCode(), reply.getMessage());
//
//        // promise不存在，说明已经提前被移除
//        final ThingPromise<ThingReply<ThingConfig>> promise = messenger.reply(token);
//        if (null == promise) {
//            logger.warn("{}/config/pull receive reply, but promised is not found, token={}", thing, token);
//            return;
//        }
//
//        // 通知应答失败
//        if (!reply.isOk()) {
//            promise.trySuccess(ThingReplyImpl.failure(reply));
//        }
//
//        // 通知应答成功
//        else {
//            promise.trySuccess(ThingReplyImpl.success(
//                    reply,
//                    new ThingConfigImpl(PRODUCT, thing, thing.getThingConnOpt(),
//                            reply.getData().configId,
//                            reply.getData().url,
//                            reply.getData().sign
//                    )
//            ));
//        }
//
//    }
//
//    /**
//     * 更新设备配置
//     *
//     * @param token 令牌
//     */
//    public ThingReplyFuture<ThingConfigApply> updateThingConfig() {
//        final String token = generateToken();
//        return new ThingReplyPromise<>(thing, token, executor, promise -> {
//
//            final String topic = format("/sys/%s/%s/thing/config/get", thing.getProductId(), thing.getThingId());
//            final Object message = new MapObject()
//                    .putProperty("id", token)
//                    .putProperty("version", "1.0")
//                    .putProperty("method", "thing.config.get")
//                    .enterProperty("params")
//                    /**/.putProperty("configScope", PRODUCT)
//                    /**/.putProperty("getType", "file")
//                    .exitProperty();
//
//            final ThingFuture<ThingReply<ThingConfig>> pullF = messenger.call(token, topic, message);
//
//            // 处理成功
//            pullF.onSuccess(future -> {
//
//                final ThingReply<ThingConfig> reply = future.getSuccess();
//
//                // 应答成功
//                if (!reply.isOk()) {
//                    promise.trySuccess(new ThingReplyImpl<>(reply.isOk(), reply.getCode(), reply.getMessage(), null));
//                }
//
//                // 应答失败
//                else {
//                    promise.trySuccess(new ThingReplyImpl<>(reply.isOk(), reply.getCode(), reply.getMessage(), new ThingConfigApply() {
//                        @Override
//                        public ThingConfig getThingConfig() {
//                            return reply.getData();
//                        }
//
//                        @Override
//                        public void apply() throws ThingException {
//
//                            final ThingConfigListener listener = thing.getThingConfigListener();
//                            if (null == listener) {
//                                throw new ThingException(thing, "thing is not configurable!");
//                            }
//
//                            try {
//                                listener.configThing(thing, getThingConfig());
//                            } catch (Exception cause) {
//                                throw new ThingException(thing, "apply config failure!", cause);
//                            }
//                        }
//                    }));
//                }
//
//            });
//
//            // 接受全部失败
//            promise.acceptFail(pullF);
//
//        });
//
//    }
//
//
//    /**
//     * 从平台拉取配置数据
//     */
//    static private class ThingConfigPullData {
//
//        @SerializedName("configId")
//        String configId;
//
//        @SerializedName("sign")
//        String sign;
//
//        @SerializedName("url")
//        String url;
//
//    }

}
