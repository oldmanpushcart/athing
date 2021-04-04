package com.github.ompc.athing.standard.thing;

import java.util.concurrent.Future;

/**
 * 设备Future
 *
 * @param <V> 类型
 */
public interface ThingFuture<V> extends Future<V> {

    /**
     * 获取设备
     *
     * @return 设备
     */
    Thing getThing();

    /**
     * 是否异常
     *
     * @return TRUE | FALSE
     */
    boolean isException();

    /**
     * 获取异常
     *
     * @return 异常
     */
    Throwable getException();

    /**
     * 是否成功
     *
     * @return TRUE | FALSE
     */
    boolean isSuccess();

    /**
     * 是否失败
     *
     * @return TRUE | FALSE
     */
    boolean isFailure();

    /**
     * 获取返回值
     *
     * @return 获取返回值
     */
    V getSuccess();

    /**
     * 添加监听器
     *
     * @param listener Future监听器
     * @return this
     */
    ThingFuture<V> appendListener(ThingFutureListener<V> listener);

    ThingFuture<V> onDone(ThingFutureListener.OnDone<V> listener);

    ThingFuture<V> onSuccess(ThingFutureListener.OnSuccess<V> listener);

    ThingFuture<V> onFailure(ThingFutureListener.OnFailure<V> listener);

    ThingFuture<V> onCancelled(ThingFutureListener.OnCancelled<V> listener);

    ThingFuture<V> onException(ThingFutureListener.OnException<V> listener);

    /**
     * 移除监听器
     *
     * @param listener Future监听器
     * @return this
     */
    ThingFuture<V> removeListener(ThingFutureListener<V> listener);

    ThingFuture<V> sync() throws InterruptedException;

}
