package com.github.ompc.athing.aliyun.platform.message.decoder;

import com.github.ompc.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.ompc.athing.aliyun.platform.product.ThProductMeta;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyConfigPushMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyPropertySetMessage;
import com.github.ompc.athing.standard.platform.message.ThingReplyServiceReturnMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

/**
 * 设备应答消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-9p8-2jl-sv4">设备下行指令结果</a>
 */
public class ThingReplyMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final Map<String, ThProductMeta> metas;

    /**
     * 设备应答消息解码器
     *
     * @param metas 设备产品元数据集合
     */
    public ThingReplyMessageDecoder(Map<String, ThProductMeta> metas) {
        this.metas = metas;
    }


    @Override
    public ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        // 检查是否设备应答返回消息
        if (!jmsTopic.matches("^/[^/]+/[^/]+/thing/downlink/reply/message$")) {
            return null;
        }

        // 解析JSON对象
        final JsonObject root = parser.parse(jmsMessage).getAsJsonObject();

        // 解析应答
        final Reply reply = gson.fromJson(root, Reply.class);
        Objects.requireNonNull(reply.productId);
        Objects.requireNonNull(reply.thingId);
        Objects.requireNonNull(reply.topic);
        Objects.requireNonNull(reply.token);

        // 解码应答服务调用
        if (reply.topic.matches("^/sys/[^/]+/[^/]+/thing/service/[^/]+_reply$")) {
            return new ThingMessage[]{decodeReplyServiceReturnMessage(root, reply)};
        }

        // 解码应答属性设置
        else if (reply.topic.matches("^/sys/[^/]+/[^/]+/thing/service/property/set_reply$")) {
            return new ThingMessage[]{decodeReplyPropertySetMessage(root, reply)};
        }

        // 解码应答配置推送
        else if (reply.topic.matches("/sys/[^/]+/[^/]+/thing/config/push_reply")) {
            return new ThingMessage[]{decodeReplyConfigPushMessage(root, reply)};
        }

        // 其他topic不在本次解码范畴
        else {
            return null;
        }

    }

    /**
     * 解码应答服务调用消息
     *
     * @param root  根节点
     * @param reply 应答
     * @return 应答服务调用消息
     * @throws DecodeException 解码错误
     */
    private ThingReplyServiceReturnMessage decodeReplyServiceReturnMessage(JsonObject root, Reply reply) throws DecodeException {

        // 解析service标识
        final Identifier identifier = Identifier.parseIdentity(
                reply.topic.substring(
                        reply.topic.lastIndexOf("/") + 1,
                        reply.topic.lastIndexOf("_reply")
                )
        );

        // 获取产品元数据
        final ThProductMeta pMeta = metas.get(reply.productId);
        if (null == pMeta) {
            throw new DecodeException(String.format("product: %s is not define!", reply.productId));
        }

        // 获取服务元数据
        final ThServiceMeta sMeta = pMeta.getThServiceMeta(identifier);
        if (null == sMeta) {
            throw new DecodeException(String.format("service: %s is not define in product: %s!", identifier, reply.productId));
        }

        // 解码返回值对象
        final Object returnObj = gson.fromJson(root.get("data"), sMeta.getReturnType());

        // 解码消息
        return new ThingReplyServiceReturnMessage(
                reply.productId,
                reply.thingId,
                reply.timestamp,
                reply.token,
                reply.code,
                reply.message,
                identifier,
                returnObj
        );
    }

    /**
     * 解码应答属性设置消息
     *
     * @param root  根节点
     * @param reply 应答
     * @return 应答属性设置消息
     */
    private ThingReplyPropertySetMessage decodeReplyPropertySetMessage(JsonObject root, Reply reply) {
        return new ThingReplyPropertySetMessage(
                reply.productId,
                reply.thingId,
                reply.timestamp,
                reply.token,
                reply.code,
                reply.message
        );
    }

    /**
     * 解码应答配置推送消息
     *
     * @param root  根节点
     * @param reply 应答
     * @return 应答配置推送消息
     */
    private ThingReplyConfigPushMessage decodeReplyConfigPushMessage(JsonObject root, Reply reply) {
        return new ThingReplyConfigPushMessage(
                reply.productId,
                reply.thingId,
                reply.timestamp,
                reply.token,
                reply.code,
                reply.message
        );
    }


    /**
     * 应答
     */
    private static class Reply {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("topic")
        String topic;

        @SerializedName("gmtCreate")
        long timestamp;

        @SerializedName("requestId")
        String token;

        @SerializedName("code")
        int code;

        @SerializedName("message")
        String message;

    }

}
