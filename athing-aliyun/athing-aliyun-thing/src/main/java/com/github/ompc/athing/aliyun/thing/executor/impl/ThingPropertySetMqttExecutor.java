package com.github.ompc.athing.aliyun.thing.executor.impl;

/**
 * 设备属性赋值执行器
 */
public class ThingPropertySetMqttExecutor {

//    private final Logger logger = LoggerFactory.getLogger(getClass());
//
//    private final Gson gson = GsonFactory.getGson();
//    private final JsonParser parser = new JsonParser();
//
//    private final ThingImpl thing;
//    private final ThingMessenger messenger;
//
//    public ThingPropertySetMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
//        this.thing = thing;
//        this.messenger = messenger;
//    }
//
//    @Override
//    public void init(MqttSubscriber subscriber) throws ThingException {
//        subscriber.subscribe(
//                format("/sys/%s/%s/thing/service/property/set", thing.getProductId(), thing.getThingId()),
//                this
//        );
//    }
//
//    @Override
//    public void handle(String mqttTopic, MqttMessage mqttMessage) {
//
//        final JsonObject requestJsonObject = parser.parse(new String(mqttMessage.getPayload(), UTF_8)).getAsJsonObject();
//        final String token = requestJsonObject.get("id").getAsString();
//        final Set<String> successIds = batchPropertySet(token, requestJsonObject.getAsJsonObject("params"));
//
//        final String topic = mqttTopic + "_reply";
//        final Object message = success(token,
//                encode(new HashMap<String, String>() {{
//                    put(FEATURE_KEY_PROPERTY_SET_REPLY_SUCCESS_IDS, String.join(",", successIds));
//                }})
//        );
//
//        messenger.post(topic, message)
//                .onSuccess(future -> logger.info("{}/property/set success, token={};identities={};", thing, token, successIds));
//
//    }
//
//
//    // 批量设置属性
//    private Set<String> batchPropertySet(String reqId, JsonObject paramsJsonObject) {
//        final Set<String> successIds = new LinkedHashSet<>();
//        if (null == paramsJsonObject) {
//            return successIds;
//        }
//
//        // 批量设置属性
//        paramsJsonObject.entrySet().forEach(entry -> {
//
//            final String identity = entry.getKey();
//            if (!Identifier.test(identity)) {
//                logger.warn("{}/property/set ignored, illegal identity, req={};identity={};",
//                        thing, reqId, identity);
//                return;
//            }
//
//            final Identifier identifier = Identifier.parseIdentity(identity);
//
//            // 过滤掉未提供的组件
//            final ThComStub thComStub = thing.getThComStubMap().get(identifier.getComponentId());
//            if (null == thComStub) {
//                logger.warn("{}/property/set ignored, component: {} not provided, req={};identity={};",
//                        thing, identifier.getComponentId(), reqId, identity);
//                return;
//            }
//
//            // 过滤掉未提供的属性
//            final ThPropertyMeta thPropertyMeta = thComStub.getThComMeta().getIdentityThPropertyMetaMap().get(identifier);
//            if (null == thPropertyMeta) {
//                logger.warn("{}/property/set ignored, property not provided, req={};identity={};",
//                        thing, reqId, identity);
//                return;
//            }
//
//            // 过滤掉只读属性
//            if (thPropertyMeta.isReadonly()) {
//                logger.warn("{}/property/set ignored, property is readonly, req={};identity={};",
//                        thing, reqId, identity);
//                return;
//            }
//
//            // 属性赋值
//            try {
//                thPropertyMeta.setPropertyValue(
//                        thComStub.getThingCom(),
//                        gson.fromJson(entry.getValue(), thPropertyMeta.getPropertyType())
//                );
//                successIds.add(identity);
//                logger.debug("{}/property/set success, req={};identity={};", thing, reqId, identity);
//            } catch (Throwable cause) {
//                logger.warn("{}/property/set ignored, occur error, req={};identity={};",
//                        thing, reqId, identity, cause);
//            }
//
//        });
//
//        return successIds;
//    }

}
