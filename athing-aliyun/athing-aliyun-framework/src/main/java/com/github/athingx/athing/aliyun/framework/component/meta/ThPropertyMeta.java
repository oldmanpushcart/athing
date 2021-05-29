package com.github.athingx.athing.aliyun.framework.component.meta;

import com.github.athingx.athing.aliyun.framework.component.ThComMetaHelper;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.component.annotation.ThProperty;
import com.github.athingx.athing.standard.component.util.ThComUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.athingx.athing.aliyun.framework.util.CommonUtils.isEmptyString;

/**
 * 设备组件属性元数据
 */
public class ThPropertyMeta {

    private final Identifier identifier;
    private final ThProperty anThProperty;
    private final Method getter;
    private final Method setter;

    ThPropertyMeta(String componentId, ThProperty anThProperty, Method getter, Method setter) {
        this.identifier = Identifier.toIdentifier(componentId, getThPropertyId(anThProperty, getter));
        this.anThProperty = anThProperty;
        this.getter = getter;
        this.setter = setter;
    }

    // 获取属性ID
    private String getThPropertyId(ThProperty anThProperty, Method getter) {
        return isEmptyString(anThProperty.id())
                ? ThComMetaHelper.dumpToLowerCaseUnderscore(ThComUtils.getJavaBeanPropertyName(getter.getName()))
                : anThProperty.id();
    }

    /**
     * 获取Get方法
     *
     * @return Get方法
     */
    public Method getGetter() {
        return getter;
    }

    /**
     * 获取Set方法
     *
     * @return Set方法
     */
    public Method getSetter() {
        return setter;
    }

    /**
     * 获取属性标识
     *
     * @return 属性标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getName() {
        return isEmptyString(anThProperty.name())
                ? ThComMetaHelper.getDefaultMemberName(getIdentifier())
                : anThProperty.name();
    }

    /**
     * 获取属性描述
     *
     * @return 属性描述
     */
    public String getDesc() {
        return anThProperty.desc();
    }

    /**
     * 判断属性是否必须
     *
     * @return TRUE | FALSE
     */
    public boolean isRequired() {
        return anThProperty.isRequired();
    }

    /**
     * 是否只读属性
     *
     * @return TRUE|FALSE
     */
    public boolean isReadonly() {
        return null == setter;
    }

    /**
     * 获取属性类型
     *
     * @return 属性类型
     */
    public Class<?> getPropertyType() {
        return getter.getReturnType();
    }

    /**
     * 获取属性值
     *
     * @param instance 组件实例
     * @return 属性值
     * @throws InvocationTargetException 获取属性方法调用失败
     * @throws IllegalAccessException    获取属性方法访问失败
     */
    public Object getPropertyValue(ThingCom instance) throws InvocationTargetException, IllegalAccessException {
        return getter.invoke(instance);
    }

    /**
     * 设置属性值
     *
     * @param instance      组件实例
     * @param propertyValue 属性值
     * @throws InvocationTargetException 设置属性方法调用失败
     * @throws IllegalAccessException    设置属性方法访问失败
     */
    public void setPropertyValue(ThingCom instance, Object propertyValue) throws InvocationTargetException, IllegalAccessException {
        if (isReadonly()) {
            throw new UnsupportedOperationException(String.format("property: %s is readonly!", getIdentifier()));
        }
        setter.invoke(instance, propertyValue);
    }

}
