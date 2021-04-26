package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.ThingFuture;

/**
 * 设备承诺
 *
 * @param <V> 类型
 */
public interface ThingPromise<V> extends ThingFuture<V> {

    /**
     * 承诺自身（无意义）
     *
     * @return this
     */
    ThingPromise<V> self();

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    boolean tryCancel();

    /**
     * 尝试失败
     *
     * @param cause 失败原因
     * @return TRUE | FALSE
     */
    boolean tryException(Throwable cause);

    /**
     * 尝试成功
     *
     * @param value 返回数据
     * @return TRUE | FALSE
     */
    boolean trySuccess(V value);

    /**
     * 尝试成功（设置{@code null}）
     *
     * @return TRUE | FALSE
     */
    boolean trySuccess();

    /**
     * 接受目标凭证全部结果
     *
     * @param target 目标凭证
     * @return this
     */
    ThingPromise<V> acceptDone(ThingFuture<? extends V> target);

    /**
     * 接受目标凭证全部失败
     *
     * @param target 目标凭证
     * @return this
     */
    ThingPromise<V> acceptFail(ThingFuture<?> target);

}
