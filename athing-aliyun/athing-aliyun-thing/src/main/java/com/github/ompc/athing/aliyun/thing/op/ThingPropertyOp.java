package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.ThingReplyPromise;
import com.github.ompc.athing.aliyun.thing.container.ThComStub;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.github.ompc.athing.aliyun.framework.Constants.FEATURE_KEY_PROPERTY_SET_REPLY_SUCCESS_IDS;
import static com.github.ompc.athing.aliyun.framework.util.FeatureCodec.encode;
import static com.github.ompc.athing.aliyun.thing.op.AlinkReply.success;
import static com.github.ompc.athing.aliyun.thing.op.ThingReplyImpl.empty;
import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static java.lang.String.format;

/**
 * 设备属性操作
 */
public class ThingPropertyOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingComContainer container;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final String _string;

    public ThingPropertyOp(Thing thing, ThingComContainer container, ThingExecutor executor, ThingMqttClient client, ThingMessenger messenger) throws ThingException {
        this.thing = thing;
        this.executor = executor;
        this.container = container;
        this.messenger = messenger;
        this._string = format("%s/op/property", thing);

        // 订阅属性提交应答MQTT消息
        client.syncSubscribe(
                format("/sys/%s/%s/thing/event/property/post_reply", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {
                    final AlinkReply<Map<String, String>> aReply = gson.fromJson(message.getStringData(), new TypeToken<AlinkReply<Map<String, String>>>() {
                    }.getType());

                    final ThingPromise<ThingReply<?>> promise = messenger.reply(aReply.getReqId());
                    if (null != promise) {
                        promise.trySuccess(empty(aReply));
                    } else {
                        logger.warn("{} post property reply is ignored: promise is not found, token={};",
                                ThingPropertyOp.this,
                                aReply.getReqId()
                        );
                    }

                }
        );


        // 订阅属性设置MQTT消息
        client.syncSubscribe(
                format("/sys/%s/%s/thing/service/property/set", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {
                    final JsonObject requestJsonObject = parser.parse(message.getStringData()).getAsJsonObject();
                    final String token = requestJsonObject.get("id").getAsString();
                    final Set<String> successIds = setProperties(token, requestJsonObject.getAsJsonObject("params"));

                    final String replyTopic = topic + "_reply";
                    final Object replyMessage = success(token,
                            encode(new HashMap<String, String>() {{
                                put(FEATURE_KEY_PROPERTY_SET_REPLY_SUCCESS_IDS, String.join(",", successIds));
                            }})
                    );

                    messenger.post(replyTopic, replyMessage)
                            .onSuccess(f -> logger.info("{} property set success, token={};identities={};", ThingPropertyOp.this, token, successIds));

                }
        );

    }

    @Override
    public String toString() {
        return _string;
    }


    // 批量设置属性
    private Set<String> setProperties(String token, JsonObject paramsJsonObject) {
        final Set<String> successIds = new LinkedHashSet<>();
        if (null == paramsJsonObject) {
            return successIds;
        }

        // 批量设置属性
        paramsJsonObject.entrySet().forEach(entry -> {

            final String identity = entry.getKey();
            if (!Identifier.test(identity)) {
                logger.warn("{} set property is ignored: illegal identity, token={};identity={};",
                        ThingPropertyOp.this, token, identity);
                return;
            }

            final Identifier identifier = Identifier.parseIdentity(identity);

            // 过滤掉未提供的组件
            final ThComStub thComStub = container.getThComStub(identifier.getComponentId());
            if (null == thComStub) {
                logger.warn("{} set property is ignored: component not provided, token={};identity={};",
                        ThingPropertyOp.this, token, identity);
                return;
            }

            // 过滤掉未提供的属性
            final ThPropertyMeta thPropertyMeta = thComStub.getThComMeta().getIdentityThPropertyMetaMap().get(identifier);
            if (null == thPropertyMeta) {
                logger.warn("{} set property is ignored: property not provided, token={};identity={};",
                        ThingPropertyOp.this, token, identity);
                return;
            }

            // 过滤掉只读属性
            if (thPropertyMeta.isReadonly()) {
                logger.warn("{} set property is ignored, property is readonly, token={};identity={};",
                        ThingPropertyOp.this, token, identity);
                return;
            }

            // 属性赋值
            try {
                thPropertyMeta.setPropertyValue(
                        thComStub.getThingCom(),
                        gson.fromJson(entry.getValue(), thPropertyMeta.getPropertyType())
                );

                successIds.add(identity);

                logger.debug("{} set property success, token={};identity={};",
                        ThingPropertyOp.this, token, identity);

            } catch (Throwable cause) {
                logger.warn("{} set property is ignored: invoke occur error, token={};identity={};",
                        ThingPropertyOp.this, token, identity, cause);
            }

        });

        return successIds;
    }


    // 构造报告数据：属性
    private MapObject buildingPostDataForThingComProperties(Identifier[] identifiers) throws ThingException {
        final MapObject parameterMap = new MapObject();
        for (final Identifier identifier : identifiers) {

            // 模块不存在
            final ThComStub thComStub = container.getThComStub(identifier.getComponentId());
            if (null == thComStub) {
                throw new ThingException(thing, format("component: %s not existed, identity=%s;",
                        identifier.getComponentId(),
                        identifier
                ));
            }

            // 属性元数据不存在
            final ThPropertyMeta thPropertyMeta = thComStub.getThComMeta().getThPropertyMeta(identifier);
            if (null == thPropertyMeta) {
                throw new ThingException(thing, format("property: %s not existed!",
                        identifier
                ));
            }

            // 获取属性值
            try {
                final Object propertyValue = thPropertyMeta.getPropertyValue(thComStub.getThingCom());
                parameterMap.enterProperty(identifier.getIdentity())
                        .putProperty("value", propertyValue)
                        .putProperty("time", new Date());
            }

            // 获取设备属性失败
            catch (Throwable cause) {
                throw new ThingException(
                        thing,
                        format("property: %s get value error!", identifier),
                        cause
                );
            }

        }

        return parameterMap;
    }

    /**
     * 投递属性
     *
     * @param identifiers 属性标识集合
     * @return future
     */
    public ThingReplyFuture<Void> post(Identifier[] identifiers) {
        final String token = generateToken();
        return new ThingReplyPromise<>(thing, token, executor, promise ->
                promise.acceptDone(messenger.call(
                        token,
                        format("/sys/%s/%s/thing/event/property/post", thing.getProductId(), thing.getThingId()),
                        new MapObject()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.event.property.post")
                                .putProperty("params", buildingPostDataForThingComProperties(identifiers))
                )));

    }

}
