package com.github.ompc.athing.aliyun.thing.executor.impl.config;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.util.HttpUtils;
import com.github.ompc.athing.aliyun.thing.util.StringUtils;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.config.ThingConfig;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 设备配置实现
 */
class ThingConfigImpl implements ThingConfig {

    private final Thing thing;
    private final ConfigScope scope;
    private final String version;
    private final ThingBootOption options;
    private final String configFileURL;
    private final String checksum;
    private final AtomicReference<String> configRef = new AtomicReference<>();

    /**
     * 设备配置
     *
     * @param scope         配置范围
     * @param thing         设备
     * @param options       连接选项
     * @param version       配置版本
     * @param configFileURL 配置下载地址
     * @param checksum      配置校验码
     */
    public ThingConfigImpl(ConfigScope scope, Thing thing, ThingBootOption options, String version, String configFileURL, String checksum) {
        this.scope = scope;
        this.version = version;
        this.thing = thing;
        this.options = options;
        this.configFileURL = configFileURL;
        this.checksum = checksum;
    }

    @Override
    public ConfigScope getScope() {
        return scope;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ThingFuture<String> getConfig() {
//        if (null != configRef.get()) {
//            return configRef.get();
//        }
//        synchronized (configRef) {
//            if (null != configRef.get()) {
//                return configRef.get();
//            }
//            final String config;
//            configRef.set(config = checksum(download()));
//            return config;
//        }
        return null;
    }

    private String download() throws ThingException {
        try {
            return HttpUtils.getAsString(
                    new URL(configFileURL),
                    options.getConnectTimeoutMs(),
                    options.getConfigTimeoutMs()
            );
        } catch (IOException cause) {
            throw new ThingException(thing, "download failure!");
        }
    }

    private String checksum(String config) throws ThingException {
        if (!StringUtils.signBySHA256(config).equalsIgnoreCase(checksum)) {
            throw new ThingException(thing, "checksum failure!");
        }
        return config;
    }

}
