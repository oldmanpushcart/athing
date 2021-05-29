package com.github.athingx.athing.aliyun.platform;

import com.aliyuncs.IAcsClient;
import com.github.athingx.athing.aliyun.framework.Constants;
import com.github.athingx.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.athingx.athing.aliyun.platform.product.ThProductStub;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.platform.ThingPlatform;
import com.github.athingx.athing.standard.platform.ThingPlatformException;
import com.github.athingx.athing.standard.platform.ThingTemplate;
import com.github.athingx.athing.standard.platform.domain.SortOrder;
import com.github.athingx.athing.standard.platform.domain.ThingPropertySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备平台实现
 */
class ThingPlatformImpl implements ThingPlatform {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, ThProductStub> thProductStubMap;
    private final IAcsClient client;
    private final ThingMessageConsumer thingMessageConsumer;
    private final Map<Method, Invoker> invokerCache;

    public ThingPlatformImpl(Map<String, ThProductStub> thProductStubMap, IAcsClient client, ThingMessageConsumer thingMessageConsumer) {
        this.thProductStubMap = thProductStubMap;
        this.client = client;
        this.thingMessageConsumer = thingMessageConsumer;
        this.invokerCache = buildingInvokeCache(thProductStubMap);
    }

    @Override
    public String toString() {
        return "thing-platform:/" + Constants.THING_PLATFORM_CODE;
    }

    @Override
    public void destroy() {
        if (null != client) {
            client.shutdown();
        }
        if (null != thingMessageConsumer) {
            thingMessageConsumer.close();
        }
        logger.info("{} destroy completed.", this);
    }

    // 检查产品ID是否符合预期
    private void checkInvokeProductId(String expect, String actual) {
        if (!expect.equals(actual)) {
            throw new IllegalArgumentException(
                    String.format("check invoke failure, expect product: %s, but actual: %s", expect, actual)
            );
        }
    }

    // 检查参数个数是否符合预期
    private void checkInvokeArgsCount(Object[] arguments, int expect) {
        final int actual = null == arguments ? 0 : arguments.length;
        if (expect != actual) {
            throw new IllegalArgumentException(
                    String.format("check invoke failure, expect arguments count: %d, but actual: %d", expect, actual)
            );
        }
    }

    // 构建方法调用缓存
    private Map<Method, Invoker> buildingInvokeCache(Map<String, ThProductStub> thProductStubMap) {
        final Map<Method, Invoker> invokerCache = new HashMap<>();
        thProductStubMap.forEach((productId, stub) ->
                stub.getThProductMeta().getThComMetaMap().forEach((thingComId, thComMeta) -> {

                    // 构建属性相关方法缓存
                    thComMeta.getIdentityThPropertyMetaMap().forEach((id, meta) -> {

                        // GetProperty
                        invokerCache.put(meta.getGetter(), (_productId, thingId, arguments) -> {
                            checkInvokeProductId(productId, _productId);
                            checkInvokeArgsCount(arguments, 0);
                            final ThingPropertySnapshot snapshot = stub.getPropertySnapshot(thingId, id);
                            return null != snapshot ? snapshot.getValue() : null;
                        });

                        // SetProperty
                        if (!meta.isReadonly()) {
                            invokerCache.put(meta.getSetter(), (_productId, thingId, arguments) -> {
                                checkInvokeProductId(productId, _productId);
                                checkInvokeArgsCount(arguments, 1);
                                stub.setPropertyValue(thingId, id, arguments[0]);
                                return null;
                            });
                        }

                    });

                    // 构建服务相关方法缓存
                    thComMeta.getIdentityThServiceMetaMap().forEach((id, meta) ->
                            invokerCache.put(meta.getService(), (_productId, thingId, arguments) -> {
                                checkInvokeProductId(productId, _productId);
                                return stub.service(thingId, id, arguments);
                            }));

                }));

        return invokerCache;
    }

    @Override
    public ThingTemplate getThingTemplate(String productId, String thingId) {

        // 检查模版中是否包含了定义的产品
        final ThProductStub thProductStub = thProductStubMap.get(productId);
        if (null == thProductStub) {
            throw new IllegalArgumentException(String.format("product: %s not define in template!", productId));
        }

        return new ThingTemplate() {

            /**
             * 根据组件类型找到对应组件，找到的组件必须存在且唯一
             *
             * @param type 组件类型
             * @return 设备组件
             */
            private ThComMeta getThComMetaByType(Class<? extends ThingCom> type) {
                final Set<ThComMeta> founds = thProductStub.getThProductMeta().getThComMetaMap().values().stream()
                        .filter(meta -> type.isAssignableFrom(meta.getThingComType()))
                        .collect(Collectors.toSet());

                // 匹配到不止一个组件则报错
                if (founds.size() > 1) {
                    throw new IllegalArgumentException(
                            String.format("component-type: %s not unique is product: %s, expect: 1, actual: %d",
                                    type.getName(),
                                    productId,
                                    founds.size()
                            ));
                }

                // 没有找到匹配的组件
                if (founds.isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("component-type: %s not found in product: %s",
                                    type.getName(),
                                    productId
                            ));
                }

                // 找到则返回
                return founds.iterator().next();

            }

            @Override
            public <T extends ThingCom> T getThingComponent(Class<T> type) {


                // 检查产品元数据中是否包含了定义的组件
                final ThComMeta thComMeta = getThComMetaByType(type);

                final ClassLoader loader = getClass().getClassLoader();

                final @SuppressWarnings("unchecked")
                T object = (T) Proxy.newProxyInstance(loader, new Class<?>[]{thComMeta.getThingComType()}, (proxy, method, args) -> {

                    // 规避掉Object的方法
                    if (method.getDeclaringClass().equals(Object.class)) {
                        return method.invoke(this, args);
                    }

                    final Invoker invoker = invokerCache.get(method);

                    // 不支持不在元数据中定义的方法
                    if (null == invoker) {
                        throw new UnsupportedOperationException();
                    }

                    // 调用执行
                    return invoker.invoke(productId, thingId, args);
                });

                return object;
            }

            @Override
            public void batchSetProperties(Map<Identifier, Object> propertyValueMap) throws ThingPlatformException {
                thProductStub.setPropertyValue(thingId, propertyValueMap);
            }

            @Override
            public Map<Identifier, ThingPropertySnapshot> batchGetProperties(Set<Identifier> identifiers) throws ThingPlatformException {
                return thProductStub.getPropertySnapshotMap(thingId, identifiers);
            }

            @Override
            public Iterator<ThingPropertySnapshot> iteratorForPropertySnapshot(Identifier identifier, int batch, SortOrder order) throws ThingPlatformException {
                return thProductStub.iteratorForPropertySnapshot(thingId, identifier, batch, order);
            }
        };
    }

    /**
     * 方法调用
     */
    private interface Invoker {

        /**
         * 调用
         *
         * @param productId 产品ID
         * @param thingId   设备ID
         * @param arguments 方法参数
         * @return 方法返回
         * @throws Throwable 调用出错
         */
        Object invoke(String productId, String thingId, Object[] arguments) throws Throwable;

    }

}
