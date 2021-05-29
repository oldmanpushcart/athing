package com.github.athingx.athing.aliyun.framework.component.meta;

import com.github.athingx.athing.aliyun.framework.component.ThComMetaHelper;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.annotation.ThService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.github.athingx.athing.aliyun.framework.util.CommonUtils.isEmptyString;

/**
 * 设备组件服务元数据
 */
public class ThServiceMeta {

    private final Identifier identifier;
    private final ThService anThService;
    private final Method service;
    private final ThParamMeta[] thParamMetaArray;

    ThServiceMeta(String componentId, ThService anThService, Method service, ThParamMeta[] thParamMetaArray) {
        this.identifier = Identifier.toIdentifier(componentId, getThServiceId(anThService, service));
        this.anThService = anThService;
        this.service = service;
        this.thParamMetaArray = thParamMetaArray;
    }

    private String getThServiceId(ThService anThService, Method service) {
        return isEmptyString(anThService.id())
                ? ThComMetaHelper.dumpToLowerCaseUnderscore(service.getName())
                : anThService.id();
    }

    /**
     * 获取服务方法
     *
     * @return 服务方法
     */
    public Method getService() {
        return service;
    }

    /**
     * 获取服务标识
     *
     * @return 服务标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取服务名称
     *
     * @return 服务名称
     */
    public String getName() {
        return isEmptyString(anThService.name())
                ? ThComMetaHelper.getDefaultMemberName(getIdentifier())
                : anThService.name();
    }

    /**
     * 获取服务描述
     *
     * @return 服务描述
     */
    public String getDesc() {
        return anThService.desc();
    }

    /**
     * 判断服务是否同步服务
     *
     * @return TRUE | FALSE
     */
    public boolean isSync() {
        return anThService.isSync();
    }

    /**
     * 判断属性是否必须
     *
     * @return TRUE | FALSE
     */
    public boolean isRequired() {
        return anThService.isRequired();
    }

    /**
     * 获取命名参数集合
     *
     * @return 命名参数集合
     */
    public ThParamMeta[] getThParamMetas() {
        return thParamMetaArray.clone();
    }

    /**
     * 获取服务返回类型
     *
     * @return 服务返回类型
     */
    public Class<?> getReturnType() {
        return service.getReturnType();
    }

    /**
     * 生成服务参数数组
     *
     * @param getArgument 获取命名参数值
     * @return 参数数组
     */
    private Object[] generateArgumentArray(GetArgument getArgument) {
        final Object[] arguments = new Object[thParamMetaArray.length];
        Arrays.stream(thParamMetaArray).forEach(meta
                -> arguments[meta.getIndex()] = getArgument.get(meta.getName(), meta.getType()));
        return arguments;
    }

    /**
     * 服务方法调用
     *
     * @param instance    实例对象
     * @param getArgument 获取参数
     * @return 服务返回结果
     * @throws InvocationTargetException 服务方法调用出错
     * @throws IllegalAccessException    服务方法访问出错
     */
    public Object service(Object instance, GetArgument getArgument) throws InvocationTargetException, IllegalAccessException {
        return service.invoke(instance, generateArgumentArray(getArgument));
    }

    /**
     * 获取命名参数值
     */
    public interface GetArgument {

        /**
         * 获取参数值
         *
         * @param name 参数名
         * @param type 参数类型
         * @return 参数值
         */
        Object get(String name, Class<?> type);

    }

}
