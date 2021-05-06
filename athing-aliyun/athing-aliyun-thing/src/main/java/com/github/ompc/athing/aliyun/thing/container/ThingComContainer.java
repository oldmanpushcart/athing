package com.github.ompc.athing.aliyun.thing.container;

import com.github.ompc.athing.aliyun.framework.component.ThComMetaHelper;
import com.github.ompc.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComJarClassLoader;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.boot.Disposable;
import com.github.ompc.athing.standard.thing.boot.Initializing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 设备组件容器
 */
public class ThingComContainer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final Set<ThingComMark> thingComMarkSet = new LinkedHashSet<>();
    private final Map<String, ThComStub> thComStubMap = new HashMap<>();
    private final String _string;

    public ThingComContainer(Thing thing) {
        this.thing = thing;
        this._string = String.format("%s/container", thing);
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 加载组件
     * <p>
     * PS：如果出现加载组件失败，本方法中将会主动释放掉加载所开启的所有外部资源，外部不必担心资源泄露的风险
     * </p>
     *
     * @param loaders 组件加载器
     * @throws ThingException 加载组件失败
     */
    private void loading(Set<ThingComLoader> loaders) throws ThingException {

        // 预加载组件ClassLoader，在组件加载失败时需要主动关闭ClassLoader释放资源
        final Set<ClassLoader> preClassLoaders = new LinkedHashSet<>();

        // 预加载组件集合，在没有全部加载器加载成功之前暂存
        final Set<ThingComMark> preThingComSet = new LinkedHashSet<>();

        // 预加载组件存根集合，在没有全部加载器加载成功之前暂存
        final Map<String, ThComStub> preThComStubMap = new LinkedHashMap<>();

        try {

            // 加载所有组件
            for (final ThingComLoader loader : loaders) {
                for (final ThingCom thingCom : loader.onLoad(thing.getProductId(), thing.getThingId())) {

                    // 登记ClassLoader
                    preClassLoaders.add(thingCom.getClass().getClassLoader());

                    // 登记组件
                    preThingComSet.add(new ThingComMark(thingCom));

                    // 为这个组件构建存根
                    for (final ThComMeta meta : ThComMetaHelper.getThComMetaMap(thingCom.getClass()).values()) {

                        // 检查设备组件ID是否冲突 & 注册到容器中
                        final ThComStub exist;
                        if ((exist = preThComStubMap.putIfAbsent(meta.getThingComId(), new ThComStub(meta, thingCom))) != null) {
                            throw new ThingException(thing, String.format(
                                    "duplicate component: %s, conflict: [ %s, %s ]",
                                    meta.getThingComId(),
                                    meta.getThingComType().getName(),
                                    exist.getThComMeta().getThingComType().getName()
                            ));
                        }

                        // 记录一个节点预加载成功
                        logger.debug("{} pre loading component stub, id={};type={};",
                                ThingComContainer.this,
                                meta.getThingComId(),
                                meta.getThingComType().getName()
                        );

                    }

                }

            }


        } catch (Throwable cause) {

            // 加载失败，则需要对已分配的资源进行销毁
            logger.warn("{} loading error!", ThingComContainer.this, cause);

            // 释放掉预加载已分配的ClassLoader
            preClassLoaders.stream()
                    .filter(loader -> loader instanceof ThingComJarClassLoader)
                    .forEach(loader -> {
                        try {
                            ((ThingComJarClassLoader) loader).close();
                        } catch (Exception cCause) {
                            logger.warn("{} release ClassLoader occur an negligible error when closing loader: {};",
                                    ThingComContainer.this, loader, cCause);
                        }
                    });

            // 异常继续对外抛出
            throw new ThingException(thing, "loading components occur error", cause);

        }

        // 如果顺利加载完成，将预加载的组件集合和组件存根集合注入到容器中
        thingComMarkSet.addAll(preThingComSet);
        thComStubMap.putAll(preThComStubMap);
        logger.info("{} loaded components total: {}", ThingComContainer.this, thComStubMap.size());

    }

    /**
     * 初始化标记
     *
     * @param mark 设备组件标记
     * @throws ThingException 初始化失败
     */
    private void _initializingMark(ThingComMark mark) throws ThingException {

        if (mark.tryMark(State.INITIALIZED)) {
            if (mark.component instanceof Initializing) {
                try {

                    final ClassLoader oriClassLoader = Thread.currentThread().getContextClassLoader();

                    try {
                        Thread.currentThread().setContextClassLoader(mark.component.getClass().getClassLoader());
                        ((Initializing) mark.component).initialized(thing);
                    } finally {
                        Thread.currentThread().setContextClassLoader(oriClassLoader);
                    }

                } catch (Throwable cause) {

                    // 如果初始化失败，则就地销毁
                    _destroyingMark(mark);

                    throw new ThingException(thing, "initializing component occur error!", cause);
                }
            }
        }

    }

    /**
     * 销毁标记
     *
     * @param mark 设备组件标记
     */
    private void _destroyingMark(ThingComMark mark) {

        if (mark.tryMark(State.DESTROYED)) {
            try {
                if (mark.component instanceof Disposable) {
                    ((Disposable) mark.component).destroy();
                }
            } catch (Exception cause) {
                logger.warn("{} destroy component occur an negligible error!",
                        ThingComContainer.this, cause);
            }
        }

    }

    /**
     * 初始化容器中所有可初始化的组件
     *
     * @param loaders 组件加载器集合
     * @throws ThingException 初始化失败
     */
    public void initializing(Set<ThingComLoader> loaders) throws ThingException {

        try {

            // 加载组件
            loading(loaders);

            // 初始化组件
            for (final ThingComMark mark : thingComMarkSet) {
                _initializingMark(mark);
            }

        } catch (Throwable cause) {

            // 就地销毁
            destroy();

            // 异常继续抛出
            throw new ThingException(thing, "init container failure!", cause);

        }

    }

    /**
     * 销毁容器
     */
    public void destroy() {

        // 销毁容器中所有可销毁的组件
        thingComMarkSet.forEach(this::_destroyingMark);

        // 关闭容器中所有组件库加载器
        thingComMarkSet.stream()
                .map(mark -> mark.component.getClass().getClassLoader())
                .filter(loader -> loader instanceof ThingComJarClassLoader)
                .map(loader -> (ThingComJarClassLoader) loader)
                .collect(Collectors.toSet())
                .forEach(loader -> {
                    try {
                        loader.close();
                    } catch (Exception cause) {
                        logger.warn("{} destroy container occur an negligible error when closing loader: {};",
                                ThingComContainer.this, loader, cause);
                    }
                });

        // 容器销毁完成
        logger.info("{} is destroyed!", this);

    }

    @SuppressWarnings("unchecked")
    public <T extends ThingCom> T getThingCom(Class<T> expect, boolean required) throws ThingException {

        final Set<ThingComMark> founds = thingComMarkSet.stream()
                .filter(mark -> expect.isInstance(mark.component))
                .collect(Collectors.toSet());

        // 如果必须要求拥有，找不到则报错
        if (required && founds.isEmpty()) {
            throw new ThingException(thing, "not found!");
        }

        // 找到多于一个则报错
        if (founds.size() > 1) {
            throw new ThingException(thing, String.format("not unique, expect: 1, actual: %d",
                    founds.size()
            ));
        }

        // 如果没有找到，返回空
        if (founds.isEmpty()) {
            return null;
        }

        final ThingComMark mark = founds.iterator().next();

        // 尝试进行一次初始化
        _initializingMark(mark);

        // 如果找到的组件已被销毁，
        if (mark.isDestroyed()) {
            if (required) {
                throw new ThingException(thing, "already destroyed!");
            } else {
                return null;
            }
        }

        return (T) mark.component;
    }

    @SuppressWarnings("unchecked")
    public <T extends ThingCom> Set<T> getThingComSet(Class<T> expect) {
        return thingComMarkSet.stream()
                .filter(mark -> {

                    if (!expect.isInstance(mark.component)) {
                        return false;
                    }

                    try {
                        _initializingMark(mark);
                    } catch (ThingException cause) {
                        logger.debug("{} found by expect type occur an negligible error when component initializing!",
                                ThingComContainer.this,
                                cause
                        );
                        return false;
                    }

                    return !mark.isDestroyed();

                })
                .map(mark -> (T) mark.component)
                .collect(Collectors.toSet());
    }

    /**
     * 获取设备组件存根
     *
     * @param thingComId 设备组件ID
     * @return 设备组件存根
     */
    public ThComStub getThComStub(String thingComId) {
        return thComStubMap.get(thingComId);
    }

    /**
     * 状态
     */
    private enum State {

        /**
         * 已创建
         */
        CREATED,

        /**
         * 已初始
         */
        INITIALIZED,

        /**
         * 已销毁
         */
        DESTROYED

    }

    /**
     * 设备组件标记
     */
    private static class ThingComMark {

        private final ThingCom component;
        private final AtomicReference<State> stateRef = new AtomicReference<>(State.CREATED);

        private ThingComMark(ThingCom component) {
            this.component = component;
        }

        /**
         * 尝试标记状态
         * <pre>
         * 1. 如果状态已经是目标状态，则标记失败
         * 2. 如果目标状态不可达，则标记失败
         * 3. 其他情况标记成功
         * </pre>
         *
         * @param state 期待标记的状态
         * @return 本次标记是否成功
         */
        boolean tryMark(State state) {

            // 标记为已初始化
            if (state == State.INITIALIZED) {
                return stateRef.compareAndSet(State.CREATED, State.INITIALIZED);
            }

            // 标记为已销毁
            else if (state == State.DESTROYED) {
                while (true) {
                    final State pre = stateRef.get();
                    if (pre == State.DESTROYED) {
                        break;
                    }
                    if (stateRef.compareAndSet(pre, State.DESTROYED)) {
                        return true;
                    }
                }
                return false;
            }

            // 其他状态不可被标记
            return false;

        }

        /**
         * 判断是否已被销毁
         *
         * @return TRUE | FALSE
         */
        boolean isDestroyed() {
            return stateRef.get() == State.DESTROYED;
        }

    }

}
