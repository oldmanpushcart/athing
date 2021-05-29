package com.github.athingx.athing.standard.thing;

import com.github.athingx.athing.standard.component.ThingCom;

import java.util.Set;

/**
 * 设备
 */
public interface Thing {

    /**
     * 获取平台代码
     *
     * @return 平台代码
     */
    String getPlatformCode();

    /**
     * 获取设备产品ID
     *
     * @return 设备产品ID
     */
    String getProductId();

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    String getThingId();

    /**
     * 获取设备组件
     * <p>
     * 一个组件类型下只有一个组件实例，如果出现多个则抛出异常
     * </p>
     * <p>
     * 如果想要根据组件类型匹配到多个组件实例，请使用 {@link #getThingComSet(Class)}
     * </p>
     *
     * @param expect   期待组件类型
     * @param required 是否必须
     *                 为{@code true}时，没有找到对应的组件时抛出异常
     * @param <T>      组件类型
     * @return 设备组件
     * @throws ThingException 获取组件失败
     */
    <T extends ThingCom> T getThingCom(Class<T> expect, boolean required) throws ThingException;

    /**
     * 获取设备组件集合
     *
     * @param expect 期待组件类型
     * @param <T>    组件类型
     * @return 符合类型的设备组件集合
     */
    <T extends ThingCom> Set<T> getThingComSet(Class<T> expect);

    /**
     * 获取设备操作
     *
     * @return 设备操作
     */
    ThingOp getThingOp();

    /**
     * 判断设备是否已被销毁
     *
     * @return TRUE | FALSE
     */
    boolean isDestroyed();

    /**
     * 销毁设备
     */
    void destroy();

}
