package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.container.ThComStub;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainer;
import com.github.ompc.athing.aliyun.thing.runtime.alink.ThingReplyImpl;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqttMessage;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ompc.athing.aliyun.thing.runtime.alink.ThingReplyImpl.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ThingServiceOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingComContainer container;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final String _string;

    public ThingServiceOp(Thing thing, ThingComContainer container, ThingMqtt mqtt, ThingMessenger messenger) throws ThingException {
        this.container = container;
        this.messenger = messenger;
        this._string = format("%s/op/service", thing);

        // 订阅同步服务调用消息
        mqtt.syncSubscribe(
                format("/ext/rrpc/+/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> service(true, topic, message)
        );

        // 订阅异步服务调用消息
        mqtt.syncSubscribe(
                format("/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {

                    /*
                     * FIX:
                     * 阿里云MQTT实现的BUG，文档上说明_reply只有发布权限没有订阅，但仍然还会推送_reply消息回来，
                     * 真的是无语，只能在这里进行一次过滤，就是可怜了客户端多收了一次消息
                     */
                    if (topic.endsWith("_reply")) {
                        return;
                    }

                    service(false, topic, message);
                }
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

        final JsonObject json = parser.parse(message.getStringData(UTF_8)).getAsJsonObject();
        final String token = json.get("id").getAsString();
        final String identity = parseIdentity(json);
        final String rTopic = isSync ? topic : topic + "_reply";

        // 不合法的标识值
        if (!Identifier.test(identity)) {
            messenger.post(rTopic, ThingReplyImpl.failure(token, ALINK_REPLY_REQUEST_ERROR, format("identity: %s is illegal", identity)));
            logger.warn("{} invoke failure: illegal identity, token={};identity={};", this, token, identity);
            return;
        }

        final Identifier identifier = Identifier.parseIdentity(identity);

        // 过滤掉未提供的组件
        final ThComStub thComStub = container.getThComStub(identifier.getComponentId());
        if (null == thComStub) {
            messenger.post(rTopic, ThingReplyImpl.failure(token, ALINK_REPLY_REQUEST_ERROR, format("component: %s not provided", identifier.getComponentId())));
            logger.warn("{} invoke failure: component not provided, token={};identity={};", this, token, identity);
            return;
        }

        // 过滤掉未提供的服务
        final ThServiceMeta thServiceMeta = thComStub.getThComMeta().getThServiceMeta(identifier);
        if (null == thServiceMeta) {
            messenger.post(rTopic, ThingReplyImpl.failure(token, ALINK_REPLY_SERVICE_NOT_PROVIDED, format("service: %s not provided", identity)));
            logger.warn("{} invoke failure: service is not provided, token={};identity={};", this, token, identity);
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
            messenger.post(rTopic, ThingReplyImpl.failure(token, ALINK_REPLY_PROCESS_ERROR, cause.getLocalizedMessage()));
            logger.warn("{} invoke failure: invoke error, token={};identity={};", this, token, identity, cause);
            return;
        }

        messenger.post(rTopic, ThingReplyImpl.success(token, result));
        logger.info("{} invoke success, token={};identity={};", this, token, identity);


    }

}
