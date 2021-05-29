package com.github.athingx.athing.aliyun.thing.container.loader;

import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.thing.boot.ThingComBoot;

/**
 * 设备组件引导加载器
 */
abstract public class ThingComBootLoader implements ThingComLoader {

    private final OnBoot onBoot;

    /**
     * 设备组件引导加载器
     *
     * @param onBoot 组件引导
     */
    public ThingComBootLoader(OnBoot onBoot) {
        this.onBoot = onBoot;
    }

    /**
     * 获取组件引导
     *
     * @return 组件引导
     */
    protected OnBoot getOnBoot() {
        return onBoot;
    }

    /**
     * 组件引导
     */
    public interface OnBoot {

        /**
         * 引导
         *
         * @param productId 产品ID
         * @param thingId   设备ID
         * @param boot      设备组件引导
         * @return 设备组件
         * @throws Exception 引导失败
         */
        ThingCom onBoot(String productId, String thingId, ThingComBoot boot) throws Exception;

    }

}
