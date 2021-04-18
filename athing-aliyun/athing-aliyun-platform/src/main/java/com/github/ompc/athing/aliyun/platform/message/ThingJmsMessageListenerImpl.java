package com.github.ompc.athing.aliyun.platform.message;

import com.github.ompc.athing.aliyun.platform.component.message.decoder.ThingMessageDecoder;
import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.github.ompc.athing.aliyun.platform.message.decoder.ThingPostMessageDecoder;
import com.github.ompc.athing.aliyun.platform.message.decoder.ThingReplyMessageDecoder;
import com.github.ompc.athing.aliyun.platform.product.ThProductMeta;
import com.github.ompc.athing.aliyun.platform.message.decoder.ThingStateMessageDecoder;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.github.ompc.athing.standard.platform.message.ThingMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备JMS消息监听器实现
 */
public class ThingJmsMessageListenerImpl implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<ThingMessageDecoder> decoders;
    private final ThingMessageListener listener;

    /**
     * 设备JMS消息监听器实现
     *
     * @param listener 设备消息监听器
     * @param decoders 消息解码器集合
     */
    public ThingJmsMessageListenerImpl(Map<String, ThProductMeta> productMetaMap, Set<ThingMessageDecoder> decoders, ThingMessageListener listener) {
        this.decoders = decoders;
        this.listener = listener;

        // 添加默认的解码器
        decoders.add(new ThingPostMessageDecoder(productMetaMap));
        decoders.add(new ThingReplyMessageDecoder(productMetaMap));
        decoders.add(new ThingStateMessageDecoder());

    }

    private void _onMessage(Message message) throws DecodeException {

        // 尝试解析jms-message
        final String jmsTopic;
        final String jmsMessageId;
        final String jmsMessage;
        try {
            jmsTopic = message.getStringProperty("topic");
            jmsMessageId = message.getStringProperty("messageId");
            jmsMessage = new String(message.getBody(byte[].class), UTF_8);
            logger.debug("{} receive jms-message id={};topic={};\n{}", this, jmsMessageId, jmsTopic, jmsMessage);
        } catch (JMSException cause) {
            throw new DecodeException("decode jms-message failure!", cause);
        }

        // 尝试进行解码
        for (final ThingMessageDecoder decoder : decoders) {
            try {
                final ThingMessage thingMessage = decoder.decode(jmsTopic, jmsMessageId, jmsMessage);
                if (null != thingMessage) {
                    listener.onMessage(thingMessage);
                    return;
                }
            } catch (Exception cause) {
                throw new DecodeException(String.format("decode jms-message failure! id=%s;topic=%s;", jmsMessageId, jmsTopic), cause);
            }
        }

        // 如果一个都没编码成功，则说明本次jms消息无法处理
        throw new DecodeException(String.format("decode jms-message failure: none decoded! id=%s;topic=%s;", jmsMessageId, jmsTopic));

    }

    @Override
    public void onMessage(Message jmsMessage) {

        try {
            _onMessage(jmsMessage);
        } catch (DecodeException cause) {
            logger.warn("{} handle jms-message occur error!", this, cause);
            throw new RuntimeException(cause);
        }

    }

}
