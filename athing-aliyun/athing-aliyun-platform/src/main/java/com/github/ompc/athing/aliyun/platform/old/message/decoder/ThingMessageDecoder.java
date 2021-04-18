package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.aliyun.platform.product.ThProductMeta;
import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * 设备消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html">数据格式</a>
 */
public class ThingMessageDecoder extends MessageDecoder<JmsMessageDecoder.Header, ThingMessageDecoder.Header> {

    private final Map<String, ThProductMeta> productMetaMap;

    public ThingMessageDecoder(Map<String, ThProductMeta> productMetaMap) {
        this.productMetaMap = productMetaMap;
    }

    @Override
    protected Header decodeHeader(JmsMessageDecoder.Header preHeader, JsonObject payloadJsonObject) throws DecodeException {

        final String productId = required(payloadJsonObject, "productKey").getAsString();
        final ThProductMeta productMeta = productMetaMap.get(productId);
        if (null == productMeta) {
            throw new DecodeException(String.format("product not defined, product=%s", productId));
        }
        return new Header(
                preHeader,
                productId,
                required(payloadJsonObject, "deviceName").getAsString(),
                required(payloadJsonObject, "requestId").getAsString(),
                getMemberAsLong(payloadJsonObject, "gmtCreate", System.currentTimeMillis()),
                productMeta
        );
    }

    /**
     * 设备消息头
     */
    public static class Header extends MessageDecoder.Header<JmsMessageDecoder.Header> {

        private final String productId;
        private final String thingId;
        private final String reqId;
        private final long timestamp;
        private final ThProductMeta productMeta;

        public Header(JmsMessageDecoder.Header parent, String productId, String thingId, String reqId, long timestamp, ThProductMeta productMeta) {
            super(parent);
            this.productId = productId;
            this.thingId = thingId;
            this.reqId = reqId;
            this.timestamp = timestamp;
            this.productMeta = productMeta;
        }

        public String getProductId() {
            return productId;
        }

        public String getThingId() {
            return thingId;
        }

        public String getReqId() {
            return reqId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public ThProductMeta getProductMeta() {
            return productMeta;
        }
    }

}
