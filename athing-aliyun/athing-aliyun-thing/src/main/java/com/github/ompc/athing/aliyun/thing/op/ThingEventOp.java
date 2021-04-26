package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.DefaultThingReply;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备属性操作
 */
public class ThingEventOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();
    private final String _string;

    public ThingEventOp(Thing thing, ThingMqtt mqtt, ThingMessenger messenger) throws ThingException {
        this.thing = thing;
        this.messenger = messenger;
        this._string = format("%s/op/event", thing);

        mqtt.syncSubscribe(
                format("/sys/%s/%s/thing/event/+/post_reply", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {

                    // 如果是属性的应答消息，则忽略
                    if (topic.endsWith("/thing/event/property/post_reply")) {
                        return;
                    }

                    final ThingReply<Void> reply = gson.fromJson(
                            message.getStringData(UTF_8),
                            new TypeToken<DefaultThingReply<Void>>() {
                            }.getType()
                    );

                    final ThingPromise<ThingReply<Void>> promise = messenger.reply(reply.getToken());
                    if (null != promise) {
                        promise.trySuccess(reply);
                    } else {
                        logger.warn("{} post event reply is ignored: promise is not found, token={};", this, reply.getToken());
                    }
                }
        );

    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 报告设备事件
     *
     * @param event 事件
     * @return future
     */
    public ThingReplyFuture<Void> post(ThingEvent<?> event) {
        final String identity = event.getIdentifier().getIdentity();
        final String topic = format("/sys/%s/%s/thing/event/%s/post", thing.getProductId(), thing.getThingId(), identity);
        return messenger.call(topic, token -> new MapObject()
                .putProperty("id", token)
                .putProperty("version", "1.0")
                .putProperty("method", format("thing.event.%s.post", identity))
                .enterProperty("params")
                /**/.putProperty("time", new Date(event.getOccurTimestampMs()))
                /**/.putProperty("value", event.getData())
                .exitProperty());
    }

}
