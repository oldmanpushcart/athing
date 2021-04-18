package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.aliyun.framework.util.FeatureCodec;
import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 应答类消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html#title-9p8-2jl-sv4">设备下行指令结果</a>
 */
public class ThingReplyMessageDecoder extends MessageDecoder<ThingMessageDecoder.Header, ThingReplyMessageDecoder.Header> {

    @Override
    protected boolean matches(ThingMessageDecoder.Header preHeader) {
        return preHeader.getParent().getMessageTopic().matches("^/[^/]+/[^/]+/thing/downlink/reply/message$");
    }

    /**
     * 解析应答Feature字符串
     *
     * @param replyCode          应答码
     * @param replyFeatureString 应答Feature字符串
     * @return 应答FeatureMap
     */
    private Map<String, String> parseReplyFeatureMap(int replyCode, String replyFeatureString) {

        final Map<String, String> featureMap = new HashMap<>();

        // 如果不是成功的消息，message中是错误信息，不需要解析
        // 如果message不是以特定字符串开头，说明不是feature特征字符串，不需要解析
        if (replyCode != 200//ThingCodes.OK
                || null == replyFeatureString
                || !replyFeatureString.startsWith("feature=1;")) {
            return featureMap;
        }

        return FeatureCodec.decode(replyFeatureString);
    }


    @Override
    protected Header decodeHeader(ThingMessageDecoder.Header preHeader, JsonObject payloadJsonObject) throws DecodeException {
        final String replyTopic = required(payloadJsonObject, "topic").getAsString();
        final int replyCode = required(payloadJsonObject, "code").getAsInt();
        final String replyMessage = getMemberAsString(payloadJsonObject, "message", "success");
        return new Header(
                preHeader,
                replyTopic,
                replyCode,
                replyMessage,
                parseReplyFeatureMap(replyCode, replyMessage)
        );
    }

    public static class Header extends MessageDecoder.Header<ThingMessageDecoder.Header> {

        private final String replyTopic;
        private final int replyCode;
        private final String replyMessage;
        private final Map<String, String> replyFeatureMap;

        public Header(ThingMessageDecoder.Header header, String replyTopic, int replyCode, String replyMessage, Map<String, String> replyFeatureMap) {
            super(header);
            this.replyTopic = replyTopic;
            this.replyCode = replyCode;
            this.replyMessage = replyMessage;
            this.replyFeatureMap = replyFeatureMap;
        }

        public String getReplyTopic() {
            return replyTopic;
        }

        public int getReplyCode() {
            return replyCode;
        }

        public String getReplyMessage() {
            return replyMessage;
        }

        public Map<String, String> getReplyFeatureMap() {
            return replyFeatureMap;
        }
    }

}
