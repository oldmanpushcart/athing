package com.github.athingx.athing.standard.thing.boot;

import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.thing.Thing;

/**
 * 设备组件生命周期
 */
public interface ThingComLifeCycle extends ThingCom, Initializing, Disposable {

    /**
     * 组件初始化
     *
     * @param thing 设备
     * @throws Exception 初始化失败
     */
    void onInitialized(Thing thing) throws Exception;

    /**
     * 组件销毁
     */
    void onDestroyed();

    /**
     * 设备网络连接
     */
    void onConnected();

    /**
     * 设备网络断开
     */
    void onDisconnected();

}
