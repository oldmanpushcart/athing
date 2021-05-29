package com.github.athingx.athing.aliyun.thing.container.loader;

import com.github.athingx.athing.standard.component.ThingCom;

/**
 * 设备组件加载器
 */
public interface ThingComLoader {

    /**
     * 加载设备组件
     *
     * @param productId 产品ID
     * @param thingId   设备ID
     * @return 设备组件数组
     * @throws Exception 加载失败
     */
    ThingCom[] onLoad(String productId, String thingId) throws Exception;

}
