package com.github.ompc.athing.aliyun.thing.executor.impl.config;

import com.github.ompc.athing.aliyun.framework.util.FeatureCodec;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.executor.impl.AlinkReplyImpl;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.github.ompc.athing.aliyun.framework.Constants.FEATURE_KEY_PUSH_CONFIG_REPLY_CONFIG_ID;
import static com.github.ompc.athing.aliyun.framework.Constants.FEATURE_KEY_PUSH_CONFIG_REPLY_SIGN;
import static com.github.ompc.athing.aliyun.thing.executor.impl.AlinkReplyImpl.ALINK_REPLY_REQUEST_ERROR;
import static com.github.ompc.athing.aliyun.thing.executor.impl.AlinkReplyImpl.success;
import static com.github.ompc.athing.standard.thing.config.ThingConfig.ConfigScope.PRODUCT;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备配置推送执行器
 */
public class ThingConfigPushMqttExecutor implements MqttExecutor, MqttExecutor.MqttMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingImpl thing;
    private final ThingMessenger messenger;
    private final Gson gson = GsonFactory.getGson();

    public ThingConfigPushMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
        this.thing = thing;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {
        subscriber.subscribe(
                format("/sys/%s/%s/thing/config/push", thing.getProductId(), thing.getThingId()),
                this
        );
    }

    @Override
    public void handle(String mqttTopic, MqttMessage mqttMessage) {

        final PushConfig pushConfig = gson.fromJson(new String(mqttMessage.getPayload(), UTF_8), PushConfig.class);
        final String token = pushConfig.id;
        final String replyTopic = format("/sys/%s/%s/thing/config/push_reply", thing.getProductId(), thing.getThingId());
        final ThingConfigListener listener = thing.getThingConfigListener();
        logger.debug("{}/config/push received, token={};version={};", thing, token, pushConfig.version);

        // 如果设备没有实现可配置接口，则忽略消息
        if (null == listener) {
            logger.warn("{}/config/push received, but thing is not configurable! token={};", thing, token);
            messenger.post(replyTopic, AlinkReplyImpl.failure(token, ALINK_REPLY_REQUEST_ERROR, "thing is not configurable!"));
            return;
        }

        final String version = pushConfig.params.configId;
        final String configURL = pushConfig.params.url;
        final String configCHS = pushConfig.params.sign;
        try {

            // 应用配置
            listener.configThing(
                    thing,
                    new ThingConfigImpl(PRODUCT, thing, thing.getThingConnOpt(), version, configURL, configCHS)
            );

        } catch (Exception cause) {
            logger.warn("{}/config/push failure, token={};version={};", thing, token, version, cause);
            messenger.post(replyTopic, AlinkReplyImpl.failure(token, ALINK_REPLY_REQUEST_ERROR, cause.getLocalizedMessage()));
            return;
        }

        final Object message = success(token, FeatureCodec.encode(
                new HashMap<String, String>() {{
                    put(FEATURE_KEY_PUSH_CONFIG_REPLY_CONFIG_ID, version);
                    put(FEATURE_KEY_PUSH_CONFIG_REPLY_SIGN, configCHS);
                }}
        ));

        // 应答配置结果
        messenger.post(replyTopic, message)
                .onSuccess(future -> logger.info("{}/config/push success. token={};scope={};version={};",
                        thing, token, PRODUCT, version));

    }

    /**
     * 从平台推送配置数据
     */
    private static class PushConfig {

        @SerializedName("id")
        final String id;

        @SerializedName("version")
        final String version;

        @SerializedName("method")
        final String method;

        @SerializedName("params")
        final Params params;

        PushConfig(String id, String version, String method, Params params) {
            this.id = id;
            this.version = version;
            this.method = method;
            this.params = params;
        }

        static class Params {

            @SerializedName("configId")
            final String configId;

            @SerializedName("configSize")
            final long configSize;

            @SerializedName("sign")
            final String sign;

            @SerializedName("signMethod")
            final String signMethod;

            @SerializedName("url")
            final String url;

            @SerializedName("getType")
            final String getType;

            Params(String configId, long configSize, String sign, String signMethod, String url, String getType) {
                this.configId = configId;
                this.configSize = configSize;
                this.sign = sign;
                this.signMethod = signMethod;
                this.url = url;
                this.getType = getType;
            }
        }

    }


}
