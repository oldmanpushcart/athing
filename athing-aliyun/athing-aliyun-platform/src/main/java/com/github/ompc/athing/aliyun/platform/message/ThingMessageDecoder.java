package com.github.ompc.athing.aliyun.platform.message;

import com.github.ompc.athing.standard.platform.message.ThingMessage;

/**
 * 设备消息解码器
 *
 * @see <a href="https://help.aliyun.com/document_detail/73736.html">数据格式</a>
 */
public interface ThingMessageDecoder {

    /**
     * 解码
     *
     * @param jmsTopic     JMS消息主题
     * @param jmsMessageId JMS消息ID
     * @param jmsMessage   JMS消息
     * @return 设备消息
     * @throws Exception 解码错误
     */
    ThingMessage[] decode(String jmsTopic, String jmsMessageId, String jmsMessage) throws Exception;

}
