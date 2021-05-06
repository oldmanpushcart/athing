package com.github.ompc.athing.standard.thing.boot;

import com.github.ompc.athing.standard.component.ThingCom;

/**
 * 可销毁
 * <p>
 * 标记一个设备组件可被销毁，用于设备销毁时会主动销毁设备组件
 * </p>
 */
public interface Disposable extends ThingCom {

    /**
     * 销毁组件
     */
    void onDestroyed();

}
