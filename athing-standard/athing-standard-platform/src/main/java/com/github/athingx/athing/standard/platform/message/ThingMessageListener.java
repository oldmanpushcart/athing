package com.github.athingx.athing.standard.platform.message;

/**
 * 设备消息监听器
 */
public interface ThingMessageListener {

    /**
     * 处理设备消息
     *
     * @param message 设备消息
     * @throws Exception 设备消息处理失败
     */
    void onMessage(ThingMessage message) throws Exception;

}
