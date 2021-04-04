package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.framework.util.IOUtils;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComBootLoader.OnBoot;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComJarBootLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;
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
     * @param remote 设备服务地址
     * @param access 设备连接密钥
     * @return Connecting
     */
    public Connecting connecting(String remote, ThingAccess access) {
        return new Connecting() {

            private final Set<ThingComLoader> thingComLoaders = new LinkedHashSet<>();
            private MqttClientFactory mcFactory = new DefaultMqttClientFactory();
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
            public Connecting setMqttClientFactory(MqttClientFactory mcFactory) {
                this.mcFactory = mcFactory;
                return this;
            }

            @Override
            public ThingFuture<Thing> connect(ThingConnectOption thingConnOpts) {

                final ThingImpl thing = new ThingImpl(
                        remote,
                        access,
                        mcFactory,
                        thingConfigListener,
                        thingOpHook,
                        thingConnOpts,
                        thingComLoaders
                );

                return new ThingPromise<Thing>(thing, promise -> {

                    thing.init();
                    promise.acceptFailure(thing.connect().onSuccess(future -> promise.trySuccess(thing)));

                }) {
                    @Override
                    public boolean tryException(Throwable cause) {
                        return super.tryException(new ThingException(thing, "connect occur error!", cause));
                    }
                };

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
         * 设置MQTT客户端工厂
         *
         * @param mcFactory MQTT客户端工厂
         * @return this
         */
        Connecting setMqttClientFactory(MqttClientFactory mcFactory);

        /**
         * 设备连接
         *
         * @param thingConnOpts 连接选项
         * @return 连接future
         */
        ThingFuture<Thing> connect(ThingConnectOption thingConnOpts);

    }

}
