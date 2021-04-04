package com.github.ompc.athing.aliyun.thing.container;

import com.github.ompc.athing.aliyun.framework.component.ThComMetaHelper;
import com.github.ompc.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComJarClassLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.aliyun.thing.util.DependentSet;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingComContainer;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.boot.Disposable;
import com.github.ompc.athing.standard.thing.boot.Initializing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备组件容器实现
 */
public class ThingComContainerImpl implements ThingComContainer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String productId;
    private final String thingId;

    private final Set<ThingCom> thingComponents = new LinkedHashSet<>();
    private final Map<String, ThComStub> thComStubMap = new HashMap<>();

    public ThingComContainerImpl(String productId, String thingId) {
        this.productId = productId;
        this.thingId = thingId;
    }

    // 容器加载组件
    protected void loading(Set<ThingComLoader> thingComLoaders) throws ThingException {

        try {

            // 加载所有组件
            final DependentSet<ThingCom> dependents = new DependentSet<>();
            for (final ThingComLoader thingComLoader : thingComLoaders) {
                dependents.addAll(Arrays.asList(thingComLoader.onLoad(productId, thingId)));
            }

            // 注入依赖并根据依赖重新排序
            new ThDependInjector().inject(productId, thingId, dependents);
            thingComponents.addAll(dependents);


            // 针对拥有ThCom注解的组件构建存根
            for (final ThingCom thingComponent : thingComponents) {
                for (final ThComMeta meta : ThComMetaHelper.getThComMetaMap(thingComponent.getClass()).values()) {

                    // 检查设备组件ID是否冲突 & 注册到容器中
                    final ThComStub exist;
                    if ((exist = thComStubMap.putIfAbsent(meta.getThingComId(), new ThComStub(meta, thingComponent))) != null) {
                        throw new ThingException(productId, thingId, String.format(
                                "duplicate component: %s, conflict: [ %s, %s ]",
                                meta.getThingComId(),
                                meta.getThingComType().getName(),
                                exist.getThComMeta().getThingComType().getName()
                        ));
                    }

                }
            }

            // 容器加载完成
            logger.info("thing:/{}/{}/container loaded components cnt: {} ",
                    productId,
                    thingId,
                    thingComponents.size()
            );

        } catch (Exception cause) {

            // 任何一个环节出错都必须导致容器被销毁，释放初始化过程中所申请的所有资源
            destroyContainer();

            // 继续对外抛出
            throw new ThingException(productId, thingId, "container loading components occur error", cause);

        }

    }

    /**
     * 初始化容器
     *
     * @param thing 设备
     * @throws ThingException 初始化失败
     */
    protected void initContainer(Thing thing) throws ThingException {

        final Set<Initializing> components = new LinkedHashSet<>();
        thingComponents.stream()
                .filter(component -> component instanceof Initializing)
                .map(component -> (Initializing) component)
                .forEach(components::add);

        for (final Initializing component : components) {
            try {
                component.initialized(thing);
            } catch (Exception cause) {
                throw new ThingException(thing, "initializing component occur error!", cause);
            }
        }

    }

    /**
     * 销毁容器
     */
    protected void destroyContainer() {

        // 销毁容器中所有可销毁的组件
        thingComponents.stream()
                .filter(component -> component instanceof Disposable)
                .forEach(component -> {

                    try {
                        ((Disposable) component).destroy();
                    } catch (Exception cause) {
                        logger.warn("thing:/{}/{}/container destroy container occur an negligible error when destroy;",
                                productId, thingId, cause);
                    }

                });

        // 关闭容器中所有组件库加载器
        thingComponents.stream()
                .map(component -> component.getClass().getClassLoader())
                .filter(loader -> loader instanceof ThingComJarClassLoader)
                .collect(Collectors.toSet())
                .forEach(loader -> {
                    try {
                        ((ThingComJarClassLoader) loader).close();
                    } catch (Exception cause) {
                        logger.warn("thing:/{}/{}/container destroy container occur an negligible error when closing loader: {};",
                                productId, thingId, loader, cause);
                    }
                });

        // 容器销毁完成
        logger.info("thing:/{}/{}/container destroy completed.",
                productId,
                thingId
        );

    }


    /**
     * 获取设备组件存根集合
     *
     * @return 设备组件存根集合
     */
    public Map<String, ThComStub> getThComStubMap() {
        return thComStubMap;
    }

    @Override
    public <T extends ThingCom> T getThingComponent(Class<T> expect, boolean required) throws ThingException {
        final Set<T> founds = getThingComponents(expect);

        // 如果必须要求拥有，找不到则报错
        if (required && founds.isEmpty()) {
            throw new ThingException(productId, thingId, "not found!");
        }

        // 找到多于一个则报错
        if (founds.size() > 1) {
            throw new ThingException(productId, thingId, String.format("not unique, expect: 1, actual: %d",
                    founds.size()
            ));
        }

        return founds.isEmpty() ? null : founds.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ThingCom> Set<T> getThingComponents(Class<T> expect) {
        return thingComponents.stream()
                .filter(expect::isInstance)
                .map(component -> (T) component)
                .collect(Collectors.toSet());
    }
}
