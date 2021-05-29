package com.github.athingx.athing.aliyun.thing.runtime;

import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqtt;

/**
 * 设备运行环境
 */
public interface ThingRuntime {

    /**
     * 获取设备信使
     *
     * @return 设备信使
     */
    ThingMessenger getThingMessenger();

    /**
     * 获取设备MQTT
     *
     * @return 设备MQTT
     */
    ThingMqtt getThingMqtt();

    /**
     * 获取设备接入
     *
     * @return 设备接入
     */
    ThingAccess getThingAccess();

    /**
     * 获取设备执行器
     *
     * @return 设备执行器
     */
    ThingExecutor getThingExecutor();

}
