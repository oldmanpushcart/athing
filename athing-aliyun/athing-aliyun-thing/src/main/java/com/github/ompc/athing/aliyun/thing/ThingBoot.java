package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.aliyun.framework.util.IOUtils;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComBootLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComJarBootLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.config.ThingConfigListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 设备启动器
 */
public class ThingBoot {

    private static final Logger logger = LoggerFactory.getLogger(ThingBoot.class);

    static {
        logger.info(IOUtils.getLogo("athing-logo.txt"));
    }

    private final URI remote;
    private final ThingAccess access;
    private final Set<ThingComLoader> loaders = new LinkedHashSet<>();

    /**
     * 设备启动参数
     */
    private ThingBootOption option = new ThingBootOption();

    /**
     * 线程池
     */
    private Executor executor;


    public ThingBoot(URI remote, ThingAccess access) {
        this.remote = Objects.requireNonNull(remote, "remote");
        this.access = Objects.requireNonNull(access, "access");
    }

    public ThingBoot load(ThingComLoader... loaders) {
        this.loaders.addAll(Arrays.asList(loaders));
        return this;
    }

    public ThingBoot load(ThingCom... components) {
        return load((productId, thingId) -> components);
    }

    public ThingBoot load(File file, ThingComBootLoader.OnBoot onBoot) {
        return load(new ThingComJarBootLoader(file, onBoot));
    }

    public ThingBoot load(File file) {
        return load(file, (productId, thingId, boot) -> boot.bootUp(productId, thingId, null));
    }

    public ThingBoot option(ThingBootOption option) {
        this.option = option;
        return this;
    }

    public ThingBoot executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public Thing boot() throws ThingException {

        final ThingImpl thing = new ThingImpl(remote, access, option, executor);
        try {

            // 初始化设备
            return thing.init(loaders);

        } catch (Throwable cause) {

            // 初始化失败，需要主动销毁设备释放已分配的资源
            thing.destroy();

            // 继续对外抛出
            throw new ThingException(thing, "boot failure!", cause);

        }
    }

}
