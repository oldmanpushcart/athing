package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.ThingReplyPromise;
import com.github.ompc.athing.aliyun.thing.mqtt.ThingMqttClient;
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
import java.util.Map;

import static com.github.ompc.athing.aliyun.thing.op.ThingReplyImpl.empty;
import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static java.lang.String.format;

/**
 * 设备属性操作
 */
public class ThingEventOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingExecutor executor;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();
    private final String _string;

    public ThingEventOp(Thing thing, ThingExecutor executor, ThingMqttClient client, ThingMessenger messenger) throws ThingException {
        this.thing = thing;
        this.executor = executor;
        this.messenger = messenger;
        this._string = format("%s/op/event", thing);

        client.syncSubscribe(
                format("/sys/%s/%s/thing/event/+/post_reply", thing.getProductId(), thing.getThingId()),
                (topic, message) -> {
                    // 如果是属性的应答消息，则忽略
                    if (topic.endsWith("/thing/event/property/post_reply")) {
                        return;
                    }

                    final AlinkReply<Map<String, String>> aReply = gson.fromJson(message.getStringData(), new TypeToken<AlinkReply<Map<String, String>>>() {
                    }.getType());

                    final ThingPromise<ThingReply<?>> promise = messenger.reply(aReply.getReqId());
                    if (null != promise) {
                        promise.trySuccess(empty(aReply));
                    } else {
                        logger.warn("{} post event reply is ignored: promise is not found, token={};",
                                ThingEventOp.this,
                                aReply.getReqId()
                        );
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
        final String token = generateToken();
        return ThingPromise.fulfill(new ThingReplyPromise<>(thing, token, executor), promise -> {
            final String identity = event.getIdentifier().getIdentity();
            final String topic = format("/sys/%s/%s/thing/event/%s/post", thing.getProductId(), thing.getThingId(), identity);
            final Object message = new MapObject()
                    .putProperty("id", token)
                    .putProperty("version", "1.0")
                    .putProperty("method", format("thing.event.%s.post", identity))
                    .enterProperty("params")
                    /**/.putProperty("time", new Date(event.getOccurTimestampMs()))
                    /**/.putProperty("value", event.getData())
                    .exitProperty();

            promise.acceptDone(messenger.call(token, topic, message));
        });

    }

}
