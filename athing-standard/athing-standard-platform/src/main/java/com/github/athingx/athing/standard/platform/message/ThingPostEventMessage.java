package com.github.athingx.athing.standard.platform.message;

import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingEvent;

import static com.github.athingx.athing.standard.platform.message.ThingMessage.Type.THING_POP_EVENT;

/**
 * 设备事件上报消息
 */
public class ThingPostEventMessage extends ThingPostMessage {

    private final Identifier identifier;
    private final ThingEvent.Data data;
    private final long occurTimestamp;

    /**
     * 设备事件上报消息
     *
     * @param productId      产品ID
     * @param thingId        设备ID
     * @param timestamp      消息时间戳
     * @param reqId          请求ID
     * @param identifier     事件标识
     * @param data           事件数据
     * @param occurTimestamp 事件发生时间戳
     */
    public ThingPostEventMessage(
            String productId, String thingId, long timestamp,
            String reqId,
            Identifier identifier, ThingEvent.Data data, long occurTimestamp
    ) {
        super(THING_POP_EVENT, productId, thingId, timestamp, reqId);
        this.identifier = identifier;
        this.data = data;
        this.occurTimestamp = occurTimestamp;
    }

    /**
     * 获取事件标识
     *
     * @return 事件标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取事件数据
     *
     * @param <T> 数据类型
     * @return 事件数据
     */
    @SuppressWarnings("unchecked")
    public <T extends ThingEvent.Data> T getData() {
        return (T) data;
    }

    /**
     * 获取事件发生时间戳
     *
     * @return 事件发生时间戳
     */
    public long getOccurTimestamp() {
        return occurTimestamp;
    }

}