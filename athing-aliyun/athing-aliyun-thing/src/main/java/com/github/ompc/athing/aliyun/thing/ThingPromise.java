package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备承诺
 *
 * @param <V> 类型
 */
public class ThingPromise<V> extends NotifiablePromise<V> {

    private final static AtomicInteger sequencer = new AtomicInteger(100000);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String _string;

    /**
     * 设备承诺
     *
     * @param thing 设备
     */
    public ThingPromise(Thing thing, Executor executor) {
        super(thing, executor);
        this._string = String.format("%s/promise/%d", thing, sequencer.getAndIncrement());
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 返回自己，无意义
     *
     * @return this
     */
    public ThingPromise<V> self() {
        return this;
    }

    /**
     * 接受目标凭证所有结果
     *
     * @param target 目标凭证
     * @return this
     */
    public ThingPromise<V> acceptDone(ThingFuture<? extends V> target) {
        target.onDone(future -> {
            // success
            if (future.isSuccess()) {
                if (trySuccess(future.getSuccess())) {
                    logger.debug("{} accept {} success", ThingPromise.this, target);
                }
            }
            // exception
            else if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.debug("{} accept {} exception", ThingPromise.this, target);
                }
            }
            // cancel
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.debug("{} accept {} cancelled", ThingPromise.this, target);
                }
            }
        });
        return this;
    }

    /**
     * 接受目标凭证所有失败
     *
     * @param target 目标凭证
     * @return this
     */
    public ThingPromise<V> acceptFail(ThingFuture<?> target) {
        target.onFailure(future -> {
            // exception
            if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.debug("{} accept {} exception", ThingPromise.this, target);
                }
            }
            // cancel
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.debug("{} accept {} cancelled", ThingPromise.this, target);
                }
            }
        });
        return this;
    }

    /**
     * 承诺履约
     *
     * @param <V> 类型
     */
    @FunctionalInterface
    public interface Fulfill<V> {

        /**
         * 履约承诺
         *
         * @param promise 设备承诺
         * @throws Throwable 履约失败
         */
        void fulfilling(ThingPromise<V> promise) throws Throwable;

    }


    /**
     * 创建设备契约并履约
     *
     * @param thing    设备
     * @param executor 执行器
     * @param fulfill  履约
     * @param <V>      类型
     * @return 设备契约
     */
    public static <V> ThingPromise<V> fulfill(Thing thing, Executor executor, Fulfill<V> fulfill) {
        return fulfill(new ThingPromise<>(thing, executor), fulfill);
    }

    /**
     * 设备契约履约
     *
     * @param promise 契约
     * @param fulfill 履约
     * @param <V>     类型
     * @param <T>     契约类型
     * @return 设备契约
     */
    public static <V, T extends ThingPromise<V>> T fulfill(T promise, Fulfill<V> fulfill) {
        promise.getExecutor().execute(() -> {
            try {
                fulfill.fulfilling(promise);
            } catch (Throwable cause) {
                promise.tryException(cause);
            }
        });
        return promise;
    }

}
