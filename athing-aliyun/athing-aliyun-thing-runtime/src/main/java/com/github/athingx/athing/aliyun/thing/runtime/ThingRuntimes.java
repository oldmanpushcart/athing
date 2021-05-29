package com.github.athingx.athing.aliyun.thing.runtime;

import com.github.athingx.athing.standard.thing.Thing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备运行环境操作
 */
public class ThingRuntimes {

    private final static Map<Thing, ThingRuntime> runtimes = new ConcurrentHashMap<>();

    /**
     * 获取设备的运行环境
     *
     * @param thing 设备
     * @return 运行环境
     */
    public static ThingRuntime getThingRuntime(Thing thing) {
        return runtimes.get(thing);
    }

    /**
     * 移除设备运行环境
     *
     * @param thing 设备
     */
    public static void remove(Thing thing) {
        runtimes.remove(thing);
    }

    /**
     * 添加设备运行环境
     *
     * @param thing   设备
     * @param runtime 运行环境
     */
    public static void append(Thing thing, ThingRuntime runtime) {
        runtimes.put(thing, runtime);
    }

}
