package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.container.ThComStub;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttMessage;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ompc.athing.aliyun.thing.op.AlinkReply.*;
import static com.github.ompc.athing.aliyun.thing.op.ThingMessenger.MQTT_QOS_AT_LEAST_ONCE;
import static com.github.ompc.athing.aliyun.thing.op.ThingMessenger.MQTT_QOS_AT_MOST_ONCE;
import static java.lang.String.format;

public class ThingServiceOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingComContainer container;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final String _string;

    public ThingServiceOp(Thing thing, ThingComContainer container, ThingMqttClient client, ThingMessenger messenger) throws ThingException {
        this.container = container;
        this.messenger = messenger;
        this._string = format("%s/op/service", thing);

        // 订阅同步服务调用消息
        client.syncSubscribe(
                format("/ext/rrpc/+/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> service(true, topic, message)
        );

        // 订阅异步服务调用消息
        client.syncSubscribe(
                format("/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> service(false, topic, message)
        );

    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 解析服务标识
     *
     * @param json JSON
     * @return 服务标识
     */
    private String parseIdentity(JsonObject json) {
        final String method = json.get("method").getAsString();
        if (null == method || method.isEmpty()) {
            throw new IllegalArgumentException(format("illegal method=%s", method));
        }
        return method.replaceFirst("thing\\.service\\.", "");
    }

    private void service(boolean isSync, String topic, ThingMqttMessage message) {

        final JsonObject json = parser.parse(message.getStringData()).getAsJsonObject();
        final String token = json.get("id").getAsString();
        final String identity = parseIdentity(json);
        final String rTopic = isSync ? topic : topic + "_reply";
        final int qos = isSync ? MQTT_QOS_AT_MOST_ONCE : MQTT_QOS_AT_LEAST_ONCE;

        // 不合法的标识值
        if (!Identifier.test(identity)) {
            messenger.post(rTopic, qos, failure(token, ALINK_REPLY_REQUEST_ERROR, format("identity: %s is illegal", identity)));
            logger.warn("{} invoke failure: illegal identity, token={};identity={};", ThingServiceOp.this, token, identity);
            return;
        }

        final Identifier identifier = Identifier.parseIdentity(identity);

        // 过滤掉未提供的组件
        final ThComStub thComStub = container.getThComStub(identifier.getComponentId());
        if (null == thComStub) {
            messenger.post(rTopic, qos, failure(token, ALINK_REPLY_REQUEST_ERROR, format("component: %s not provided", identifier.getComponentId())));
            logger.warn("{} invoke failure: component not provided, token={};identity={};", ThingServiceOp.this, token, identity);
            return;
        }

        // 过滤掉未提供的服务
        final ThServiceMeta thServiceMeta = thComStub.getThComMeta().getThServiceMeta(identifier);
        if (null == thServiceMeta) {
            messenger.post(rTopic, qos, failure(token, ALINK_REPLY_SERVICE_NOT_PROVIDED, format("service: %s not provided", identity)));
            logger.warn("{} invoke failure: service is not provided, token={};identity={};", ThingServiceOp.this, token, identity);
            return;
        }

        // 执行服务调用
        final Object result;
        try {
            final JsonObject argumentJson = json.get("params").getAsJsonObject();
            result = thServiceMeta.service(
                    thComStub.getThingCom(),
                    (name, type) -> gson.fromJson(argumentJson.get(name), type)
            );
        } catch (Throwable cause) {
            messenger.post(rTopic, qos, failure(token, ALINK_REPLY_PROCESS_ERROR, cause.getLocalizedMessage()));
            logger.warn("{} invoke failure: invoke error, token={};identity={};", ThingServiceOp.this, token, identity, cause);
            return;
        }

        messenger.post(rTopic, qos, success(token, "success", result));
        logger.info("{} invoke success, token={};identity={};", ThingServiceOp.this, token, identity);


    }

}
