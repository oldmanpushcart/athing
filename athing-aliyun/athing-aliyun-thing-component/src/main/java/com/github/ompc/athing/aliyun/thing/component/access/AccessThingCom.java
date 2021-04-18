package com.github.ompc.athing.aliyun.thing.component.access;

import com.github.ompc.athing.standard.component.ThingCom;

/**
 * 设备接入组件
 */
public interface AccessThingCom extends ThingCom {

    /**
     * 获取设备接入
     *
     * @return 设别接入
     */
    Access getAccess();

}
