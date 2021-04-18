package com.github.ompc.athing.aliyun.platform.old.message.decoder;

import com.github.ompc.athing.aliyun.platform.message.decoder.DecodeException;
import com.github.ompc.athing.standard.platform.message.ThingMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 消息解码器
 */
abstract public class MessageDecoder<PRE, HEADER extends MessageDecoder.Header<?>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Collection<MessageDecoder<HEADER, ?>> decoders = new ArrayList<>();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    /**
     * 解码事件
     *
     * @param preHeader          前序消息头
     * @param payloadJsonElement 消息载荷
     * @return 事件
     * @throws DecodeException 解码事件失败
     */
    final public ThingMessage decodeThingMessage(PRE preHeader, JsonElement payloadJsonElement) throws DecodeException {

        logger.trace("enter decoder:{} -> {}", this, payloadJsonElement);

        // 判断是否需要处理
        if (!matches(preHeader)) {
            logger.trace("exit decoder by not matches, decoder={}", this);
            return null;
        }

        final JsonObject payloadJsonObject = payloadJsonElement.getAsJsonObject();

        // 解码消息头
        // 如果解码不出，则说明已经不是当前解码链能继续处理，需要中断解码链路
        final HEADER header = decodeHeader(preHeader, payloadJsonObject);
        if (null == header) {
            logger.trace("exit decoder by header is null, decoder={}", this);
            return null;
        }

        // 解码消息载荷
        // 如果解码不出，则说明已经不是当前解码链能处理，需要中断解码链路
        final ThingMessage thingMessage = decodePayload(header, payloadJsonObject);
        if (null != thingMessage) {
            logger.trace("exit decoder by decoded, decoder={};message={};", this, thingMessage);
            return thingMessage;
        }

        // 当前解码器解码不出，交给下一个解码器处理
        for (final MessageDecoder<HEADER, ?> decoder : decoders) {

            final ThingMessage nextThingMessage = decoder.decodeThingMessage(header, payloadJsonObject);
            if (nextThingMessage != null) {
                logger.trace("exit decoder by decoded (by next), decoder={};message={};", this, nextThingMessage);
                return nextThingMessage;
            }

        }

        // 所有解码器均失败，解码失败
        logger.trace("exit decoder by failure, decoder={};", this);
        return null;
    }


    /**
     * 是否匹配当前解码器
     *
     * @param preHeader 前序消息头
     * @return TRUE | FALSE
     */
    protected boolean matches(PRE preHeader) {
        return true;
    }

    /**
     * 解码消息头
     *
     * @param preHeader         前序消息头
     * @param payloadJsonObject 前序消息载荷
     * @return 消息头
     * @throws DecodeException 解码消息头失败
     */
    abstract protected HEADER decodeHeader(PRE preHeader, JsonObject payloadJsonObject) throws DecodeException;

    /**
     * 解码消息载荷
     *
     * @param header            消息头
     * @param payloadJsonObject 前序消息载荷
     * @return 消息载荷
     * @throws DecodeException 解码载荷失败
     */
    protected ThingMessage decodePayload(HEADER header, JsonObject payloadJsonObject) throws DecodeException {
        return null;
    }

    /**
     * 设置下一解码节点
     *
     * @param decoder 下一解码节点
     * @return this
     */
    final public MessageDecoder<PRE, HEADER> next(MessageDecoder<HEADER, ?> decoder) {
        this.decoders.add(decoder);
        return this;
    }

    final protected JsonElement required(JsonObject objectJsonObject, String member) throws DecodeException {
        final JsonElement memberJsonElement = objectJsonObject.get(member);
        if (null == memberJsonElement) {
            throw new DecodeException(String.format("%s is required in %s", member, objectJsonObject));
        }
        return memberJsonElement;
    }

    final protected String getMemberAsString(JsonObject objectJsonObject, String member, String value) {
        return objectJsonObject.has(member)
                ? objectJsonObject.get(member).getAsString()
                : value;
    }

    final protected long getMemberAsLong(JsonObject objectJsonObject, String member, long value) {
        return objectJsonObject.has(member)
                ? objectJsonObject.get(member).getAsLong()
                : value;
    }

    /**
     * 消息头
     *
     * @param <PARENT> 前序消息类型
     */
    public static class Header<PARENT> {

        private final PARENT parent;

        public Header(PARENT parent) {
            this.parent = parent;
        }

        /**
         * 获取前序消息头
         *
         * @return 前序消息头
         */
        public PARENT getParent() {
            return parent;
        }

    }

}
