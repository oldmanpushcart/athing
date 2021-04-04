package com.github.ompc.athing.aliyun.thing.executor.impl.config;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.aliyun.thing.ThingReplyPromise;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.executor.impl.AlinkReplyImpl;
import com.github.ompc.athing.aliyun.thing.executor.impl.ThingReplyImpl;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingReply;
import com.github.ompc.athing.standard.thing.ThingReplyFuture;
import com.github.ompc.athing.standard.thing.config.ThingConfig;
import com.github.ompc.athing.standard.thing.config.ThingConfigApply;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static com.github.ompc.athing.standard.thing.config.ThingConfig.ConfigScope.PRODUCT;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备配置主动请求执行器
 */
public class ThingConfigPullMqttExecutor implements MqttExecutor, MqttExecutor.MqttMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingImpl thing;
    private final ThingMessenger messenger;

    private final Gson gson = GsonFactory.getGson();

    public ThingConfigPullMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
        this.thing = thing;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {
        subscriber.subscribe(
                format("/sys/%s/%s/thing/config/get_reply", thing.getProductId(), thing.getThingId()),
                this
        );
    }

    @Override
    public void handle(String mqttTopic, MqttMessage mqttMessage) {

        final AlinkReplyImpl<ThingConfigPullData> reply = gson.fromJson(
                new String(mqttMessage.getPayload(), UTF_8),
                new TypeToken<AlinkReplyImpl<ThingConfigPullData>>() {
                }.getType()
        );

        final String token = reply.getReqId();
        logger.debug("{}/config/pull receive reply, req={};code={};message={};",
                thing, token, reply.getCode(), reply.getMessage());

        // promise不存在，说明已经提前被移除
        final ThingPromise<ThingReply<ThingConfig>> promise = messenger.reply(token);
        if (null == promise) {
            logger.warn("{}/config/pull receive reply, but promise is not found, token={}", thing, token);
            return;
        }

        // 通知失败
        if (!reply.isOk()) {
            promise.trySuccess(ThingReplyImpl.failure(reply));
            return;
        }

        // 通知成功
        promise.trySuccess(ThingReplyImpl.success(
                reply,
                new ThingConfigImpl(PRODUCT, thing, thing.getThingConnOpt(),
                        reply.getData().configId,
                        reply.getData().url,
                        reply.getData().sign
                )
        ));
    }

    /**
     * 更新设备配置
     */
    public ThingReplyFuture<ThingConfigApply> updateThingConfig() {
        final String token = generateToken();
        return new ThingReplyPromise<>(thing, token, promise -> {


            final String topic = format("/sys/%s/%s/thing/config/get", thing.getProductId(), thing.getThingId());

            final ThingFuture<ThingReply<ThingConfig>> pullF = messenger.call(token, topic, new MapObject()
                    .putProperty("id", token)
                    .putProperty("version", "1.0")
                    .putProperty("method", "thing.config.get")
                    .enterProperty("params")
                    /**/.putProperty("configScope", PRODUCT)
                    /**/.putProperty("getType", "file")
                    .exitProperty()
            );

            pullF.onSuccess(future -> {

                final ThingReply<ThingConfig> reply = future.getSuccess();
                if (!reply.isOk()) {
                    promise.trySuccess(new ThingReplyImpl<>(reply.isOk(), reply.getCode(), reply.getMessage(), null));
                } else {
                    promise.trySuccess(new ThingReplyImpl<>(reply.isOk(), reply.getCode(), reply.getMessage(), new ThingConfigApply() {
                        @Override
                        public ThingConfig getThingConfig() {
                            return reply.getData();
                        }

                        @Override
                        public void apply() throws ThingException {

                            final ThingConfigListener listener = thing.getThingConfigListener();
                            if (null == listener) {
                                throw new ThingException(thing, "thing is not configurable!");
                            }

                            try {
                                listener.configThing(thing, getThingConfig());
                            } catch (Exception cause) {
                                throw new ThingException(thing, "apply config failure!", cause);
                            }
                        }
                    }));
                }

            });

            promise.acceptFailure(pullF);

        });

    }


    /**
     * 从平台拉取配置数据
     */
    static private class ThingConfigPullData {

        @SerializedName("configId")
        String configId;

        @SerializedName("sign")
        String sign;

        @SerializedName("url")
        String url;

    }

}
