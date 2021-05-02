package com.github.ompc.athing.standard.thing;

import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;

/**
 * 设备操作
 */
public interface ThingOp {

    /**
     * 报告设备事件
     *
     * @param event 事件
     * @return 设备应答Future
     */
    ThingReplyFuture<Void> postEvent(ThingEvent<?> event);

    /**
     * 报告设备属性
     *
     * @param identifiers 设备属性标识
     * @return 设备应答Future
     */
    ThingReplyFuture<Void> postProperties(Identifier[] identifiers);

    /**
     * MQTT客户端连接
     *
     * @return 连接凭证
     */
    ThingFuture<ThingConnection> connect();

}
