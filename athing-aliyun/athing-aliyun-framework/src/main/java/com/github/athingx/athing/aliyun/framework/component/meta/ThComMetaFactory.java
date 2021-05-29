package com.github.athingx.athing.aliyun.framework.component.meta;

import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.component.annotation.*;
import com.github.athingx.athing.standard.component.util.ThComUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 设备组件元数据工厂
 */
public class ThComMetaFactory {

    // 生成标识服务元数据集合
    private static Map<Identifier, ThServiceMeta> generateIdentityThServiceMetaMap(String thingComId, Class<?> intf) {
        final Map<Identifier, ThServiceMeta> identityServiceMetaMap = new HashMap<>();
        Stream.of(intf.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ThService.class))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .forEach(service -> {

                    final ThService anThService = service.getDeclaredAnnotation(ThService.class);

                    // 构建参数名数组
                    final ThParamMeta[] thParamMetaArray = new ThParamMeta[service.getParameterCount()];
                    for (int index = 0; index < thParamMetaArray.length; index++) {

                        final Parameter parameter = service.getParameters()[index];

                        // ThService方法的所有参数都必须要有@ThParamName注解
                        if (!parameter.isAnnotationPresent(ThParam.class)) {
                            throw new IllegalArgumentException(String.format(
                                    "parameter[%d] require @ThParam at %s#%s()",
                                    index,
                                    service.getDeclaringClass().getName(),
                                    service.getName()
                            ));
                        }

                        final ThParam anThParam = parameter.getDeclaredAnnotation(ThParam.class);
                        thParamMetaArray[index] = new ThParamMeta(anThParam, parameter.getType(), index);

                    }

                    // 构建服务元数据
                    final ThServiceMeta meta = new ThServiceMeta(
                            thingComId,
                            anThService,
                            service,
                            thParamMetaArray
                    );

                    identityServiceMetaMap.put(meta.getIdentifier(), meta);

                });

        return identityServiceMetaMap;
    }

    // 生成标识属性元数据集合
    private static Map<Identifier, ThPropertyMeta> generateIdentityThPropertyMetaMap(String thingComId, Class<?> intf) {
        final Map<Identifier, ThPropertyMeta> identityThPropertyMetaMap = new HashMap<>();
        Stream.of(intf.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ThProperty.class))
                .filter(ThComUtils::isJavaBeanPropertyGetMethod)
                .forEach(getter -> {

                    final ThProperty anThProperty = getter.getDeclaredAnnotation(ThProperty.class);
                    final String propertyName = ThComUtils.getJavaBeanPropertyName(getter.getName());
                    final Class<?> propertyType = getter.getReturnType();

                    // 尝试寻找匹配的setter
                    final Method setter = Stream.of(intf.getDeclaredMethods())
                            .filter(method -> ThComUtils.isJavaBeanPropertySetMethod(method, propertyName, propertyType))
                            .findFirst()
                            .orElse(null);

                    // 构建属性元数据
                    final ThPropertyMeta meta = new ThPropertyMeta(thingComId, anThProperty, getter, setter);
                    identityThPropertyMetaMap.put(meta.getIdentifier(), meta);

                });

        return identityThPropertyMetaMap;
    }

    // 生成设备组件事件元数据集合
    private static Map<Identifier, ThEventMeta> generateIdentityThEventMetaMap(String thingComId, Class<?> intf) {
        return Stream.of(intf.getAnnotationsByType(ThEvent.class))
                .map(anThEvent -> new ThEventMeta(thingComId, anThEvent))
                .collect(Collectors.toMap(
                        ThEventMeta::getIdentifier,
                        meta -> meta,
                        (a, b) -> b));
    }

    /**
     * 从{@link ThingCom}接口中生成设备组件元数据
     *
     * @param intf {@link ThingCom}组件接口
     * @return 设备组件元数据
     */
    public static ThComMeta make(Class<?> intf) {
        // 必须是一个接口
        if (!intf.isInterface()) {
            throw new IllegalArgumentException("require interface !");
        }

        // 必须是ThingCom接口的子接口
        if (!ThingCom.class.isAssignableFrom(intf)) {
            throw new IllegalArgumentException(String.format("require extends %s",
                    ThingCom.class.getName())
            );
        }

        // 必须标注了@ThCom
        if (!intf.isAnnotationPresent(ThCom.class)) {
            throw new IllegalArgumentException("require @ThCom");
        }

        final ThCom anThCom = intf.getAnnotation(ThCom.class);
        final String thingComId = anThCom.id();
        return new ThComMeta(
                anThCom,
                intf,
                generateIdentityThEventMetaMap(thingComId, intf),
                generateIdentityThPropertyMetaMap(thingComId, intf),
                generateIdentityThServiceMetaMap(thingComId, intf)
        );
    }

}
