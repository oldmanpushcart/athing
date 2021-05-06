package com.github.ompc.athing.standard.thing.boot;

import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.Thing;

/**
 * 可初始化
 * <p>
 * 用于标记一个设备组件可被初始化，
 * 被标记的设备组件在设备初始化完成后，进行初始化
 * </p>
 */
public interface Initializing extends ThingCom {

    /**
     * 设备组件初始化
     * <p>
     * 通知组件设备初始化完成，组件可以通过实现这个方法正式开始一个组件的工作
     * </p>
     *
     * @param thing 完成初始化的设备
     * @throws Exception 初始化失败
     */
    void onInitialized(Thing thing) throws Exception;

}
