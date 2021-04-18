package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;

/**
 * JMS消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/143601.html">AMPQ Java SDK接入示例</a>
 */
public class JmsMessageDecoder extends MessageDecoder<Message, JmsMessageDecoder.Header> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Header decodeHeader(Message message, JsonObject payloadJsonObject) throws DecodeException {
        try {
            return new Header(
                    message,
                    message.getStringProperty("topic"),
                    message.getStringProperty("messageId")
            );
        } catch (Exception cause) {
            throw new DecodeException("decode jms-message header occur error!", cause);
        }
    }

    @Override
    protected ThingMessage decodePayload(Header header, JsonObject payloadJsonObject) throws DecodeException {
        return super.decodePayload(header, payloadJsonObject);
    }

    /**
     * JMS消息头
     */
    public static class Header extends MessageDecoder.Header<Message> {

        private final String messageTopic;
        private final String messageId;

        /**
         * 构建JMS消息头
         *
         * @param message      JMS消息
         * @param messageTopic 消息主题
         * @param messageId    消息ID
         */
        public Header(Message message, String messageTopic, String messageId) {
            super(message);
            this.messageTopic = messageTopic;
            this.messageId = messageId;
        }

        public String getMessageTopic() {
            return messageTopic;
        }

        public String getMessageId() {
            return messageId;
        }
    }

}
