package com.github.ompc.athing.aliyun.thing.executor.impl;

import com.github.ompc.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.ThingReplyPromise;
import com.github.ompc.athing.aliyun.thing.container.ThComStub;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备上报平台执行器
 */
public class ThingPostMqttExecutor implements MqttExecutor, MqttExecutor.MqttMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThingImpl thing;
    private final ThingMessenger messenger;
    private final Gson gson = GsonFactory.getGson();
    private final Type tokenType = new TypeToken<AlinkReplyImpl<Map<String, String>>>() {
    }.getType();

    public ThingPostMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
        this.thing = thing;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {
        subscriber.subscribe(
                format("/sys/%s/%s/thing/event/+/post_reply", thing.getProductId(), thing.getThingId()),
                this
        );
    }

    @Override
    public void handle(String mqttTopic, MqttMessage mqttMessage) {

        // 消息内容
        final String message = new String(mqttMessage.getPayload(), UTF_8);

        // 解析alink应答数据
        final AlinkReplyImpl<Map<String, String>> reply = gson.fromJson(message, tokenType);

        // 请求ID
        final String token = reply.getReqId();
        logger.debug("{}/post reply received, token={};topic={};message={};", thing, token, mqttTopic, message);

        // 拿到应答的promise
        final ThingPromise<ThingReply<?>> replyF = messenger.reply(token);
        if (null == replyF) {
            logger.warn("{}/post reply received, but promise is not found, token={};topic={};",
                    thing,
                    token,
                    mqttTopic
            );
            return;
        }

        // 属性上报的应答需要做特殊日志处理
        if (mqttTopic.endsWith("/thing/event/property/post_reply")
                && null != reply.getData()
                && !reply.getData().isEmpty()) {
            logger.warn("{}/property/post reply, but some properties failure, req={};properties={};",
                    thing,
                    token,
                    reply.getData()
            );
        }

        // 应答
        replyF.trySuccess(ThingReplyImpl.empty(reply));

    }

    /**
     * 报告设备事件
     *
     * @param event 事件
     * @return future
     */
    public ThingReplyFuture<Void> postThingEvent(ThingEvent<?> event) {

        final String token = generateToken();
        final String identity = event.getIdentifier().getIdentity();
        return new ThingReplyPromise<Void>(thing, token, promise ->
                promise.accept(messenger.call(
                        token,
                        format("/sys/%s/%s/thing/event/%s/post", thing.getProductId(), thing.getThingId(), identity),
                        new MapObject()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", format("thing.event.%s.post", identity))
                                .enterProperty("params")
                                /**/.putProperty("time", new Date(event.getOccurTimestampMs()))
                                /**/.putProperty("value", event.getData())
                                .exitProperty()))) {

            @Override
            public boolean tryException(Throwable cause) {
                return super.tryException(new ThingException(
                        thing,
                        format("post event error, identity=%s;", identity),
                        cause
                ));
            }

        };

    }

    // 构造报告数据：属性
    private MapObject buildingPostDataForThingComProperties(Identifier[] identifiers) throws ThingException {
        final MapObject parameterMap = new MapObject();
        for (final Identifier identifier : identifiers) {
            // 模块不存在
            final ThComStub thComStub = thing.getThComStubMap().get(identifier.getComponentId());
            if (null == thComStub) {
                throw new ThingException(thing, String.format("component: %s not existed, identity=%s;",
                        identifier.getComponentId(),
                        identifier
                ));
            }

            // 属性元数据不存在
            final ThPropertyMeta thPropertyMeta = thComStub
                    .getThComMeta()
                    .getIdentityThPropertyMetaMap()
                    .get(identifier);
            if (null == thPropertyMeta) {
                throw new ThingException(thing, String.format("property: %s not existed!",
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
                        String.format("property: %s get value error!", identifier),
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
    public ThingReplyFuture<Void> postThingProperties(Identifier[] identifiers) {

        final String token = generateToken();
        return new ThingReplyPromise<Void>(thing, token, promise ->
                promise.accept(messenger.call(
                        token,
                        format("/sys/%s/%s/thing/event/property/post", thing.getProductId(), thing.getThingId()),
                        new MapObject()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.event.property.post")
                                .putProperty("params", buildingPostDataForThingComProperties(identifiers))
                ))) {

            @Override
            public boolean tryException(Throwable cause) {
                return super.tryException(new ThingException(
                        thing,
                        String.format("post properties error, identities=%s", Arrays.asList(identifiers)),
                        cause
                ));
            }

        };

    }

}
