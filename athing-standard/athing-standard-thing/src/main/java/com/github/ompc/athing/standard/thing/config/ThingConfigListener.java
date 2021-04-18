package com.github.ompc.athing.standard.thing.config;

import com.github.ompc.athing.standard.thing.Thing;

/**
 * 设备配置监听器
 */
public interface ThingConfigListener {

    /**
     * 不支持的操作
     */
    ThingConfigListener unsupported = (thing, config) -> {
        throw new UnsupportedOperationException();
    };

    /**
     * 配置设备
     *
     * @param thing  设备
     * @param config 设备配置
     * @throws Exception 配置设备出错
     */
    void configThing(Thing thing, ThingConfig config) throws Exception;

}
