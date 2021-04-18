package com.github.ompc.athing.standard.thing.config;

import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;

/**
 * 设备配置
 */
public interface ThingConfig {

    /**
     * 获取配置范围
     *
     * @return 配置范围
     */
    ConfigScope getScope();

    /**
     * 获取配置版本
     *
     * @return 配置版本
     */
    String getVersion();

    /**
     * 获取配置内容
     *
     * @return 获取凭证
     */
    ThingFuture<String> getConfig();

    /**
     * 配置范围
     */
    enum ConfigScope {

        /**
         * 产品
         */
        PRODUCT

    }
}
