package com.github.athingx.athing.aliyun.thing.op;

import com.github.athingx.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.athingx.athing.aliyun.framework.util.GsonFactory;
import com.github.athingx.athing.aliyun.thing.container.ThComStub;
import com.github.athingx.athing.aliyun.thing.container.ThingComContainer;
import com.github.athingx.athing.aliyun.thing.runtime.caller.ThingCaller;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqttMessage;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static com.github.athingx.athing.aliyun.thing.runtime.messenger.JsonSerializerImpl.serializer;
import static com.github.athingx.athing.aliyun.thing.runtime.messenger.alink.ThingReplyImpl.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ThingServiceOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingComContainer container;
    private final ThingMessenger messenger;
    private final ThingCaller caller;

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final String _string;

    public ThingServiceOp(Thing thing, ThingComContainer container, ThingCaller caller, ThingMqtt mqtt, ThingMessenger messenger) throws ThingException {
        this.container = container;
        this.caller = caller;
        this.messenger = messenger;
        this._string = format("%s/op/service", thing);

        // ??????????????????????????????
        mqtt.syncSubscribe(
                format("/ext/rrpc/+/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> service(true, topic, message)
        );

        // ??????????????????????????????
        mqtt.syncSubscribe(
                format("/sys/%s/%s/thing/service/+", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {

                    /*
                     * FIX:
                     * ?????????MQTT?????????BUG??????????????????_reply??????????????????????????????????????????????????????_reply???????????????
                     * ???????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ??????????????????
     *
     * @param json JSON
     * @return ????????????
     */
    private String parseIdentity(JsonObject json) {
        final String method = json.get("method").getAsString();
        if (null == method || method.isEmpty()) {
            throw new IllegalArgumentException(format("illegal method=%s", method));
        }
        return method.replaceFirst("thing\\.service\\.", "");
    }

    /**
     * ????????????
     *
     * @param isSync  ??????????????????
     * @param topic   ??????Topic
     * @param message ????????????
     */
    private void service(boolean isSync, String topic, ThingMqttMessage message) {

        final JsonObject json = parser.parse(message.getStringData(UTF_8)).getAsJsonObject();
        final String token = json.get("id").getAsString();
        final String identity = parseIdentity(json);
        final String rTopic = isSync ? topic : topic + "_reply";

        // ?????????????????????
        if (!Identifier.test(identity)) {
            messenger.post(serializer, rTopic, failure(token, ALINK_REPLY_REQUEST_ERROR, format("identity: %s is illegal", identity)));
            logger.warn("{} invoke failure: illegal identity, token={};identity={};", this, token, identity);
            return;
        }

        final Identifier identifier = Identifier.parseIdentity(identity);

        // ???????????????????????????
        final ThComStub thComStub = container.getThComStub(identifier.getComponentId());
        if (null == thComStub) {
            messenger.post(serializer, rTopic, failure(token, ALINK_REPLY_REQUEST_ERROR, format("component: %s not provided", identifier.getComponentId())));
            logger.warn("{} invoke failure: component not provided, token={};identity={};", this, token, identity);
            return;
        }

        // ???????????????????????????
        final ThServiceMeta thServiceMeta = thComStub.getThComMeta().getThServiceMeta(identifier);
        if (null == thServiceMeta) {
            messenger.post(serializer, rTopic, failure(token, ALINK_REPLY_SERVICE_NOT_PROVIDED, format("service: %s not provided", identity)));
            logger.warn("{} invoke failure: service is not provided, token={};identity={};", this, token, identity);
            return;
        }

        caller.call(promise -> {
            final JsonObject argumentJson = json.get("params").getAsJsonObject();
            try {
                promise.trySuccess(
                        thServiceMeta.service(
                                thComStub.getThingCom(),
                                (name, type) -> gson.fromJson(argumentJson.get(name), type)
                        )
                );
            } catch (InvocationTargetException itCause) {
                throw itCause.getCause();
            }
        })
                .onSuccess(future -> {
                    messenger.post(serializer, rTopic, success(token, future.get()));
                    logger.info("{} invoke success, token={};identity={};", this, token, identity);
                })
                .onFailure(future -> {
                    messenger.post(serializer, rTopic, failure(token, ALINK_REPLY_PROCESS_ERROR, future.getException().getLocalizedMessage()));
                    logger.warn("{} invoke failure: invoke error, token={};identity={};", this, token, identity, future.getException());
                });

    }

}
