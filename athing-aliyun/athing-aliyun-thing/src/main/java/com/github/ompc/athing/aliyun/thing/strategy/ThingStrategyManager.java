package com.github.ompc.athing.aliyun.thing.strategy;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备策略管理器
 */
public class ThingStrategyManager {

    private final Set<ThingStrategy> strategies = new LinkedHashSet<>();

    /**
     * 注册设备策略
     *
     * @param strategy 设备策略
     */
    public void register(ThingStrategy strategy) {
        strategies.add(strategy);
    }

    /**
     * 删除设备策略
     *
     * @param strategy 设备策略
     */
    private void remove(ThingStrategy strategy) {
        strategies.remove(strategy);
    }

    /**
     * 获取指定类型的策略集合
     *
     * @param type 指定策略类型
     * @param <T>  策略类型
     * @return 指定类型策略集合
     */
    @SuppressWarnings("unchecked")
    public <T extends ThingStrategy> Set<T> getThingStrategies(Class<T> type) {
        return strategies.stream()
                .filter(type::isInstance)
                .map(strategy -> (T) strategy)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
