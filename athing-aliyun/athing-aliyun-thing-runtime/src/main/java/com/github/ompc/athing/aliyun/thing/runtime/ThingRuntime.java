package com.github.ompc.athing.aliyun.thing.runtime;

import com.github.ompc.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.ompc.athing.aliyun.thing.runtime.alink.Alink;
import com.github.ompc.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.runtime.logger.ThingLogger;
import com.github.ompc.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.runtime.mqtt.ThingMqtt;

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

    /**
     * 获取设备日志
     *
     * @param clazz 类
     * @return 日志
     */
    ThingLogger getThingLogger(Class<?> clazz);

    /**
     * 获取设备日志
     *
     * @param name 日志名称
     * @return 日志
     */
    ThingLogger getThingLogger(String name);

    /**
     * 获取alink协议支撑
     *
     * @return alink协议支撑
     */
    Alink getAlink();

}
