package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.framework.util.IOUtils;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComBootLoader.OnBoot;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComJarBootLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 设备连接器
 * <p>
 * 负责将设备启动并连接到设备平台
 * </p>
 */
public class ThingConnector {

    private static final Logger logger = LoggerFactory.getLogger(ThingConnector.class);

    static {
        logger.info(IOUtils.getLogo("athing-logo.txt"));
    }


    /**
     * 连接设备平台
     *
     * @param thingServerUrl 设备服务地址
     * @param access         设备连接密钥
     * @return Connecting
     */
    public Connecting connecting(String thingServerUrl, ThingAccess access) {
        return new Connecting() {

            private final Set<ThingComLoader> thingComLoaders = new LinkedHashSet<>();
            private ThingConfigListener thingConfigListener;
            private ThingOpHook thingOpHook = thing -> {
                throw new UnsupportedOperationException();
            };

            @Override
            public Connecting load(ThingCom... thingComComponents) {
                return load((productId, thingId) -> thingComComponents);
            }

            @Override
            public Connecting load(File comJarFile, OnBoot onBoot) {
                return load(new ThingComJarBootLoader(comJarFile, onBoot));
            }

            @Override
            public Connecting load(File comJarFile) {
                return load(comJarFile, (productId, thingId, boot) -> boot.bootUp(productId, thingId, null));
            }

            @Override
            public Connecting load(ThingComLoader... loaders) {
                if (null != loaders) {
                    thingComLoaders.addAll(Arrays.asList(loaders));
                }
                return this;
            }

            @Override
            public Connecting setThingConfigListener(ThingConfigListener configListener) {
                this.thingConfigListener = configListener;
                return this;
            }

            @Override
            public Connecting setThingOpHook(ThingOpHook opHook) {
                this.thingOpHook = opHook;
                return this;
            }

            @Override
            public Thing connect(ThingConnectOption thingConnOpt) throws ThingException {
                final ThingImpl thing = new ThingImpl(
                        thingServerUrl,
                        access,
                        thingConfigListener,
                        thingOpHook,
                        thingConnOpt,
                        thingComLoaders
                );
                try {
                    thing.init();
                    thing.connect();
                } catch (Exception cause) {
                    thing.destroy();
                    throw new ThingException(thing, "connect occur error!", cause);
                }
                return thing;
            }

        };
    }

    public interface Connecting {

        /**
         * 加载设备组件
         *
         * @param thingComComponents 设备组件集合
         * @return this
         */
        Connecting load(ThingCom... thingComComponents);

        /**
         * 加载设备组件库文件
         *
         * @param comJarFile 设备组件库文件
         * @param onBoot     设备组件引导
         * @return this
         */
        Connecting load(File comJarFile, OnBoot onBoot);

        /**
         * 加载设备组件库文件
         *
         * @param comJarFile 设备组件库文件
         * @return this
         */
        Connecting load(File comJarFile);

        /**
         * 加载设备组件
         *
         * @param loaders 设备组件加载器
         * @return this
         */
        Connecting load(ThingComLoader... loaders);

        /**
         * 设置设备配置监听器
         *
         * @param configListener 设备配置监听器
         * @return this
         */
        Connecting setThingConfigListener(ThingConfigListener configListener);

        /**
         * 设置设备操作钩子
         *
         * @param opHook 操作钩子
         * @return this
         */
        Connecting setThingOpHook(ThingOpHook opHook);

        /**
         * 设备连接
         *
         * @param thingConnOpts 设备连接选项
         * @return 设备
         * @throws ThingException 连接失败
         */
        Thing connect(ThingConnectOption thingConnOpts) throws ThingException;

    }

}
