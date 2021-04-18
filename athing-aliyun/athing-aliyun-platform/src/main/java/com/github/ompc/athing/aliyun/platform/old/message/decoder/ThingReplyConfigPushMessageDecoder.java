package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyConfigPushMessage;
import com.google.gson.JsonObject;

import static com.github.ompc.athing.aliyun.framework.Constants.FEATURE_KEY_PUSH_CONFIG_REPLY_CONFIG_ID;

/**
 * 配置推送消息应答解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-9p8-2jl-sv4">设备下行指令结果</a>
 */
public class ThingReplyConfigPushMessageDecoder extends MessageDecoder<ThingReplyMessageDecoder.Header, ThingReplyMessageDecoder.Header> {

    @Override
    protected boolean matches(ThingReplyMessageDecoder.Header preHeader) {
        return preHeader.getReplyTopic().matches("/sys/[^/]+/[^/]+/thing/config/push_reply");
    }

    @Override
    protected ThingReplyMessageDecoder.Header decodeHeader(ThingReplyMessageDecoder.Header preHeader, JsonObject payloadJsonObject) {
        return preHeader;
    }

    @Override
    protected ThingMessage decodePayload(ThingReplyMessageDecoder.Header header, JsonObject payloadJsonObject) {
        return new ThingReplyConfigPushMessage(
                header.getParent().getProductId(),
                header.getParent().getThingId(),
                header.getParent().getTimestamp(),
                header.getParent().getReqId(),
                header.getReplyCode(),
                header.getReplyMessage(),
                "PRODUCT",
                header.getReplyFeatureMap().get(FEATURE_KEY_PUSH_CONFIG_REPLY_CONFIG_ID)
        );
    }
}
