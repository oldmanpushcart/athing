package com.github.ompc.athing.aliyun.thing.executor.impl;

import com.github.ompc.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.container.ThComStub;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.thing.ThingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static com.github.ompc.athing.aliyun.framework.util.GsonFactory.getEmptyIfNull;
import static com.github.ompc.athing.aliyun.thing.executor.ThingMessenger.MQTT_QOS_AT_LEAST_ONCE;
import static com.github.ompc.athing.aliyun.thing.executor.ThingMessenger.MQTT_QOS_AT_MOST_ONCE;
import static com.github.ompc.athing.aliyun.thing.executor.impl.AlinkReplyImpl.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 服务调用MQTT执行器
 */
public class ThingServiceInvokeMqttExecutor implements MqttExecutor, MqttExecutor.MqttMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ThingImpl thing;
    private final ThingMessenger messenger;

    private final JsonParser parser = new JsonParser();
    private final Gson gson = GsonFactory.getGson();

    public ThingServiceInvokeMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
        this.thing = thing;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {
        for (final String topicExpress : new LinkedHashSet<String>() {{
            // 订阅同步服务调用RRPC主题
            add(format("/ext/rrpc/+/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()));

            // 订阅异步服务调用MQTT主题
            addAll(
                    thing.getThComStubMap().values().stream()
                            .flatMap(stub -> stub.getThComMeta().getIdentityThServiceMetaMap().values().stream())
                            .filter(meta -> !meta.isSync())
                            .map(meta -> meta.getIdentifier().getIdentity())
                            .map(identifier -> format("/sys/%s/%s/thing/service/%s",
                                    thing.getProductId(),
                                    thing.getThingId(),
                                    identifier
                            ))
                            .collect(Collectors.toSet())
            );
        }}) {
            subscriber.subscribe(topicExpress, this);
        }
    }

    @Override
    public void handle(String mqttTopic, MqttMessage mqttMessage) {

        final JsonObject requestJsonObject = parser.parse(new String(mqttMessage.getPayload(), UTF_8)).getAsJsonObject();
        final ThingServiceInvoker invoker = new ThingServiceInvoker(mqttTopic, requestJsonObject);
        final String reqId = invoker.reqId;
        final String identity = invoker.identity;

        // 不合法的标识值
        if (!Identifier.test(identity)) {
            reply(invoker, ALINK_REPLY_REQUEST_ERROR, format("identity: %s is illegal", identity));
            logger.warn("{}/service illegal identity, req={};identity={};", thing, reqId, identity);
            return;
        }

        final Identifier identifier = Identifier.parseIdentity(identity);

        // 过滤掉未提供的组件
        final ThComStub thComStub = thing.getThComStubMap().get(identifier.getComponentId());
        if (null == thComStub) {
            reply(invoker, ALINK_REPLY_REQUEST_ERROR, format("component: %s not provided", identifier.getComponentId()));
            logger.warn("{}/service component is not provided, req={};identity={};", thing, reqId, identity);
            return;
        }

        // 过滤掉未提供的服务
        final ThServiceMeta thServiceMeta = thComStub.getThComMeta().getIdentityThServiceMetaMap().get(identifier);
        if (null == thServiceMeta) {
            reply(invoker, ALINK_REPLY_SERVICE_NOT_PROVIDED, format("service: %s not provided", identity));
            logger.warn("{}/service service is not provided, req={};identity={};", thing, reqId, identity);
            return;
        }

        // 执行服务调用
        final Object result;
        try {
            final JsonObject argumentJsonObject = requestJsonObject.get("params").getAsJsonObject();
            result = thServiceMeta.service(
                    thComStub.getThingCom(),
                    (name, type) -> gson.fromJson(argumentJsonObject.get(name), type)
            );
        } catch (Throwable cause) {
            reply(invoker, ALINK_REPLY_PROCESS_ERROR, cause.getLocalizedMessage());
            logger.warn("{}/service invoke error, req={};identity={};", thing, reqId, identity, cause);
            return;
        }

        reply(invoker, result);
        logger.info("{}/service invoke success, req={};identity={};", thing, reqId, identity);

    }

    /**
     * 应答
     *
     * @param invoker 服务调用者
     * @param code    alink返回码
     * @param message alink返回信息
     */
    private void reply(ThingServiceInvoker invoker, int code, String message) {
        messenger.post(invoker.getReplyTopic(), invoker.getQos(), failure(invoker.reqId, code, message));
    }

    /**
     * 应答
     *
     * @param invoker 服务调用者
     * @param result  设备组件返回
     */
    private void reply(ThingServiceInvoker invoker, Object result) {
        messenger.post(
                invoker.getReplyTopic(),
                invoker.getQos(),
                success(invoker.reqId, "success", getEmptyIfNull(result))
        );
    }


    /**
     * 设备服务调用
     */
    private static class ThingServiceInvoker {

        final String reqId;
        final String identity;
        final String topic;
        final boolean isSync;

        ThingServiceInvoker(String topic, JsonObject requestJsonObject) {

            this.topic = topic;
            this.isSync = topic.startsWith("/ext/rrpc");

            // 检查thing.service.method是否有效
            final String method = requestJsonObject.get("method").getAsString();
            if (null == method || !method.startsWith("thing.service.")) {
                throw new IllegalArgumentException(format("illegal method=%s", method));
            }

            this.identity = method.replaceFirst("thing\\.service\\.", "");

            // 检查reqId是否合法
            this.reqId = requestJsonObject.get("id").getAsString();
            if (null == this.reqId || this.reqId.isEmpty()) {
                throw new IllegalArgumentException("request-id is empty");
            }

        }

        /**
         * 是否同步服务
         *
         * @return TRUE | FALSE
         */
        boolean isSync() {
            return isSync;
        }

        /**
         * 获取应答主题
         *
         * @return 应答主题
         */
        String getReplyTopic() {
            return isSync() ? topic : topic + "_reply";
        }

        /**
         * 获取QOS
         *
         * @return QOS
         */
        int getQos() {
            return isSync() ? MQTT_QOS_AT_MOST_ONCE : MQTT_QOS_AT_LEAST_ONCE;
        }

    }

}
