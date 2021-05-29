package com.github.athingx.athing.aliyun.platform.message.decoder;

import com.github.athingx.athing.aliyun.framework.component.meta.ThEventMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.athingx.athing.aliyun.framework.util.GsonFactory;
import com.github.athingx.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.athingx.athing.aliyun.platform.product.ThProductMeta;
import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.platform.domain.ThingPropertySnapshot;
import com.github.athingx.athing.standard.platform.message.ThingMessage;
import com.github.athingx.athing.standard.platform.message.ThingPostEventMessage;
import com.github.athingx.athing.standard.platform.message.ThingPostPropertyMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * 设备批量上报属性/事件消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-e2v-s2p-hek">设备属性批量上报</a>
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-2ug-0ca-43t">设备事件批量上报</a>
 */
public class ThingBatchPostMessageDecoder implements ThingMessageDecoder {

    private final Gson gson = GsonFactory.getGson();
    private final JsonParser parser = new JsonParser();
    private final Map<String, ThProductMeta> metas;

    public ThingBatchPostMessageDecoder(Map<String, ThProductMeta> metas) {
        this.metas = metas;
    }

    @Override
    public ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception {

        if (!jmsTopic.matches("^/[^/]+/[^/]+/thing/(event|property)/batch/post$")) {
            return null;
        }

        final JsonObject root = parser.parse(jmsMessage).getAsJsonObject();
        final BatchPost post = gson.fromJson(root, BatchPost.class);

        // 检查设备产品是否定义
        if (!metas.containsKey(post.productId)) {
            throw new DecodeException(String.format("product: %s is not define!", post.productId));
        }

        // 解码批量事件上报消息
        if (jmsTopic.endsWith("thing/event/batch/post")) {
            return decodePostEventMessages(root, post);
        }

        // 解码批量属性上报消息
        else if (jmsTopic.endsWith("/thing/property/batch/post")) {
            return decodePostPropertyMessage(root, post);
        }

        return null;
    }

    /**
     * 解码批量上报事件消息
     *
     * @param root 根节点
     * @param post POST
     * @return 上报事件消息
     * @throws DecodeException 解码失败
     */
    private ThingPostEventMessage[] decodePostEventMessages(JsonObject root, BatchPost post) throws DecodeException {
        final List<ThingPostEventMessage> messages = new ArrayList<>();
        final ThProductMeta pMeta = metas.get(post.productId);

        // 解析payload
        for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("payload").entrySet()) {
            final String identity = entry.getKey();

            // 事件元数据
            final ThEventMeta meta = pMeta.getThEventMeta(identity);
            if (null == meta) {
                throw new DecodeException(String.format("event: %s is not define in product: %s", identity, post.productId));
            }

            // 解析每个事件
            for (final JsonElement itemE : entry.getValue().getAsJsonArray()) {
                final JsonObject item = itemE.getAsJsonObject();
                final long timestamp = item.get("time").getAsLong();
                final ThingEvent.Data data = gson.fromJson(item.get("value"), meta.getType());
                messages.add(new ThingPostEventMessage(
                        post.productId,
                        post.thingId,
                        timestamp,
                        post.token,
                        meta.getIdentifier(),
                        data,
                        timestamp
                ));
            }

        }
        messages.sort(Comparator.comparingLong(ThingPostEventMessage::getTimestamp));
        return messages.toArray(new ThingPostEventMessage[0]);
    }

    /**
     * 解码批量上报属性消息
     *
     * @param root 根节点
     * @param post POST
     * @return 上报属性消息
     * @throws DecodeException 解码失败
     */
    private ThingPostPropertyMessage[] decodePostPropertyMessage(JsonObject root, BatchPost post) throws DecodeException {
        final List<ThingPostPropertyMessage> messages = new ArrayList<>();
        final ThProductMeta pMeta = metas.get(post.productId);

        // 解析payload
        for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("payload").entrySet()) {
            final String identity = entry.getKey();

            // 属性元数据
            final ThPropertyMeta meta = pMeta.getThPropertyMeta(identity);
            if (null == meta) {
                throw new DecodeException(String.format("property: %s is not define in product: %s!", identity, post.productId));
            }

            // 解析每个属性
            for (final JsonElement itemE : entry.getValue().getAsJsonArray()) {
                final JsonObject item = itemE.getAsJsonObject();
                final long timestamp = item.get("time").getAsLong();
                final Object value = gson.fromJson(item.get("value"), meta.getPropertyType());
                final ThingPropertySnapshot snapshot = new ThingPropertySnapshot(meta.getIdentifier(), value, timestamp);
                messages.add(new ThingPostPropertyMessage(
                        post.productId,
                        post.thingId,
                        timestamp,
                        post.token,
                        new HashMap<String, ThingPropertySnapshot>() {{
                            put(identity, snapshot);
                        }}
                ));
            }

        }

        messages.sort(Comparator.comparingLong(ThingMessage::getTimestamp));
        return messages.toArray(new ThingPostPropertyMessage[0]);
    }


    /**
     * 批量上报
     */
    private static class BatchPost {

        @SerializedName("productKey")
        String productId;

        @SerializedName("deviceName")
        String thingId;

        @SerializedName("requestId")
        String token;

    }

}
