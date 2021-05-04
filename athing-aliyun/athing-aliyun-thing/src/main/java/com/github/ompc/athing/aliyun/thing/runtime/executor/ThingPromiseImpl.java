package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备承诺实现
 *
 * @param <V> 类型
 */
public class ThingPromiseImpl<V> extends ThingFutureImpl<V> implements ThingPromise<V> {

    private final static AtomicInteger sequencer = new AtomicInteger(100000);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String _string;

    /*
     * 通知执行线程池
     */
    private final Executor executor;

    /*
     * 等待通知集合
     */
    private final Collection<ThingFutureListener<V>> notifies = new ArrayList<>();

    /*
     * 已通知标记
     */
    private volatile boolean notified;


    /**
     * 设备承诺
     *
     * @param thing    设备
     * @param executor 执行器
     */
    public ThingPromiseImpl(Thing thing, Executor executor) {
        super(thing);
        this.executor = executor;
        this._string = String.format("%s/promise/%d", thing, sequencer.getAndIncrement());
    }

    @Override
    public String toString() {
        return _string;
    }


    /**
     * <p>重点，此处为通知核心逻辑!</p>
     *
     * @param listener 等待通知的执行器，可为空
     */
    private void notify(ThingFutureListener<V> listener) {

        // 立即通知集合
        final Collection<ThingFutureListener<V>> immediateNotifies = new ArrayList<>();

        // 监听器加入到立即通知集合，将会在本次完成通知
        if (null != listener) {
            immediateNotifies.add(listener);
        }

        // 如果是第一次通知，需要将之前等待的监听器合入
        if (!notified) {
            synchronized (this) {

                // double check
                if (!notified) {

                    // 克隆并清空原有监听器队列
                    immediateNotifies.addAll(notifies);
                    notifies.clear();

                    // 标记为已通知
                    notified = true;

                }

            }
        }

        // 立即进行通知
        if (!immediateNotifies.isEmpty()) {

            executor.execute(() -> immediateNotifies.forEach(notify -> {
                try {
                    notify.onDone(this);
                } catch (Throwable cause) {
                    logger.warn("{} notify listener error!", this, cause);
                }
            }));

        }

    }


    @Override
    public ThingFuture<V> appendListener(ThingFutureListener<V> listener) {

        // 如若从未进行过通知，则将监听器加入到等待通知集合
        if (!notified) {
            synchronized (this) {
                if (!notified) {
                    notifies.add(listener);
                    return this;
                }
            }
        }

        // 如果已通知过，则需要自行进行通知
        notify(listener);
        return this;
    }

    @Override
    public ThingFuture<V> removeListener(ThingFutureListener<V> listener) {
        synchronized (notifies) {
            notifies.remove(listener);
        }
        return this;
    }

    @Override
    public boolean tryCancel() {
        if (super.tryCancel()) {
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryException(Throwable cause) {
        if (super.tryException(cause)) {
            notify(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean trySuccess(V value) {
        if (super.trySuccess(value)) {
            notify(null);
            return true;
        }
        return false;
    }


    @Override
    public ThingPromise<V> self() {
        return this;
    }

    @Override
    public ThingPromise<V> acceptDone(ThingFuture<? extends V> target) {
        target.onDone(future -> {
            // success
            if (future.isSuccess()) {
                if (trySuccess(future.getSuccess())) {
                    logger.trace("{} accept {} success", this, target);
                }
            }
            // exception
            else if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.trace("{} accept {} exception", this, target);
                }
            }
            // cancel
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.trace("{} accept {} cancelled", this, target);
                }
            }
        });
        return this;
    }

    @Override
    public ThingPromise<V> acceptFail(ThingFuture<?> target) {
        target.onFailure(future -> {
            // exception
            if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.trace("{} accept {} exception", this, target);
                }
            }
            // cancel
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.trace("{} accept {} cancelled", this, target);
                }
            }
        });
        return this;
    }

}
