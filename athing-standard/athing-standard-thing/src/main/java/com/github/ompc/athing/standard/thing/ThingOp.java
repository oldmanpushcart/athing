package com.github.ompc.athing.standard.thing;

import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingEvent;
import com.github.ompc.athing.standard.thing.boot.Modular;
import com.github.ompc.athing.standard.thing.config.ThingConfigApply;

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
    ThingReplyFuture<Void> postThingEvent(ThingEvent<?> event);

    /**
     * 报告设备属性
     *
     * @param identifiers 设备属性标识
     * @return 设备应答Future
     */
    ThingReplyFuture<Void> postThingProperties(Identifier[] identifiers);

    /**
     * 报告模块信息
     *
     * @param module 模块
     * @return 设备应答Future
     */
    ThingTokenFuture<Void> reportModule(Modular module);

    /**
     * 更新设备配置
     *
     * @return 设备应答Future
     */
    ThingReplyFuture<ThingConfigApply> updateThingConfig();

    /**
     * 重启设备
     *
     * @return 设备应答Future
     */
    ThingFuture<Void> reboot();

}
