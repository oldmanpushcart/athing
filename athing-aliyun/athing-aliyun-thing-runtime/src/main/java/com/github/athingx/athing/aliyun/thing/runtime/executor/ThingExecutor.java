package com.github.athingx.athing.aliyun.thing.runtime.executor;

import com.github.athingx.athing.standard.thing.ThingFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 设备执行器
 */
public interface ThingExecutor extends Executor {

    /**
     * 提交执行任务
     *
     * @param task 任务
     * @param <T>  返回类型
     * @return 执行凭证
     */
    <T> ThingFuture<T> submit(Callable<T> task);

    /**
     * 提交执行任务
     *
     * @param task 任务
     * @return 执行凭证
     */
    ThingFuture<Void> submit(Runnable task);

    /**
     * 有延迟提交执行任务
     *
     * @param delay 延迟时长
     * @param unit  延迟单位
     * @param task  任务
     * @return 执行凭证
     */
    ThingFuture<Void> submit(long delay, TimeUnit unit, Runnable task);

    /**
     * 承诺
     *
     * @param <V> 类型
     * @return 设备承诺
     */
    <V> ThingPromise<V> promise();

    /**
     * 承诺
     *
     * @param fulfill 承诺履约
     * @param <V>     类型
     * @return 设备承诺
     */
    <V> ThingPromise<V> promise(Fulfill<V> fulfill);

    /**
     * 承诺
     *
     * @param promise 设备承诺
     * @param fulfill 承诺履约
     * @param <V>     类型
     * @param <T>     承诺类型
     * @return 设备承诺
     */
    <V, T extends ThingPromise<V>> T promise(T promise, Fulfill<V> fulfill);

}
